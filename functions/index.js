const functions = require('firebase-functions');
const admin = require('firebase-admin');
const crypto = require('crypto');
const { onRequest } = require('firebase-functions/v2/https');
const { onDocumentCreated } = require('firebase-functions/v2/firestore');
const { defineSecret } = require('firebase-functions/params');
admin.initializeApp();

const posApprovalApiSecret = defineSecret('POS_APPROVAL_API_SECRET');

// Normaliza role para MAIÚSCULAS (DONO, GERENTE, FUNCIONARIO, ...)
function normalizeRole(role) {
  if (!role) return '';
  return String(role).trim().toUpperCase();
}

// Pega o storeId do doc (aceita storeID ou storeId — o campo real é storeID)
function storeIdOf(data) {
  return (data && (data.storeID || data.storeId)) || '';
}

// Aplica as custom claims de um usuário: { storeID, role }
async function applyClaims(userId) {
  const db = admin.firestore();
  const userDoc = await db.collection('users').doc(userId).get();
  if (!userDoc.exists) {
    console.log(`Usuário ${userId} não encontrado na coleção 'users'`);
    return;
  }
  const data = userDoc.data();
  const storeID = storeIdOf(data);
  const claims = {
    // storeID é o nome canônico exigido pelas rules do Firestore
    storeID: storeID,
    // storeId mantido como fallback temporário de compatibilidade
    storeId: storeID,
    role: normalizeRole(data.role)
  };
  await admin.auth().setCustomUserClaims(userId, claims);
  console.log(`Custom claims definidos para ${userId}:`, claims);
}

// Roda toda vez que um usuário é CRIADO no Authentication
exports.setUserClaims = functions.auth.user().onCreate(async (user) => {
  try {
    await applyClaims(user.uid);
  } catch (error) {
    console.error('Erro ao definir claims onCreate:', error);
  }
  return null;
});

// Roda toda vez que o documento `users/{uid}` é criado/atualizado
exports.updateUserClaims = functions.firestore
  .document('users/{userId}')
  .onWrite(async (change, context) => {
    const data = change.after.data();
    if (!data) return null;
    try {
      await applyClaims(context.params.userId);
    } catch (error) {
      console.error(`Erro ao atualizar claims de ${context.params.userId}:`, error);
    }
    return null;
  });

const managerRoles = new Set(['GERENTE', 'DONO', 'MANAGER', 'OWNER']);
const approvalTypes = new Set([
  'CANCEL_PURCHASE', 'CANCEL_PRODUCT', 'REFUND', 'RETURN', 'SPECIAL_DISCOUNT'
]);

function sameSecret(candidate, expected) {
  if (typeof candidate !== 'string' || !expected) return false;
  const candidateBytes = Buffer.from(candidate, 'utf8');
  const expectedBytes = Buffer.from(expected, 'utf8');
  return candidateBytes.length === expectedBytes.length
    && crypto.timingSafeEqual(candidateBytes, expectedBytes);
}

function approvalMessage(request) {
  const sale = `venda #${request.saleId}`;
  switch (request.requestType) {
    case 'CANCEL_PURCHASE': return `Pedido para cancelar a ${sale} aguarda aprovação.`;
    case 'CANCEL_PRODUCT': return `Pedido para cancelar produto da ${sale} aguarda aprovação.`;
    case 'REFUND': return `Pedido de estorno da ${sale} aguarda aprovação.`;
    case 'RETURN': return `Pedido de devolução da ${sale} aguarda aprovação.`;
    case 'SPECIAL_DISCOUNT': return `Desconto especial na ${sale} aguarda aprovação.`;
    default: return 'Há um pedido do PDV aguardando sua aprovação.';
  }
}

async function sendApprovalToTokens(request, deviceSnapshots) {
  const devices = deviceSnapshots.filter((doc) => {
    const data = doc.data();
    return data && data.fcmToken && managerRoles.has(normalizeRole(data.role));
  });
  if (devices.length === 0) return 0;

  const message = {
    data: {
      title: 'Aprovação necessária no PDV',
      body: approvalMessage(request),
      notificationType: 'pos_approval',
      notificationId: request.requestId,
      entityId: request.requestId,
      requestId: request.requestId,
      operationType: request.requestType,
      saleId: String(request.saleId),
      storeID: String(request.storeID)
    },
    android: { priority: 'high' }
  };

  for (let start = 0; start < devices.length; start += 500) {
    const batch = devices.slice(start, start + 500);
    const response = await admin.messaging().sendEachForMulticast({
      ...message,
      tokens: batch.map((doc) => doc.data().fcmToken)
    });
    const invalid = [];
    response.responses.forEach((result, index) => {
      const code = result.error && result.error.code;
      if (code === 'messaging/invalid-registration-token'
          || code === 'messaging/registration-token-not-registered') {
        invalid.push(batch[index].ref.delete());
      }
    });
    if (invalid.length) await Promise.all(invalid);
  }
  return devices.length;
}

exports.posApprovalBridge = onRequest(
  { region: 'southamerica-east1', secrets: [posApprovalApiSecret] },
  async (req, res) => {
    if (req.method !== 'POST') return res.status(405).json({ error: 'method_not_allowed' });
    if (!sameSecret(req.get('X-Mottainai-Pos-Secret'), posApprovalApiSecret.value())) {
      return res.status(401).json({ error: 'unauthorized' });
    }

    const body = req.body || {};
    if (body.operation === 'dispatch') {
      const requestId = String(body.requestId || '');
      const storeID = String(body.storeID || '');
      if (!/^[0-9a-f-]{36}$/i.test(requestId)
          || !/^\d+$/.test(storeID)
          || !approvalTypes.has(body.requestType)
          || !Number.isInteger(Number(body.saleId))
          || !Number.isInteger(Number(body.employeeId))) {
        return res.status(400).json({ error: 'invalid_approval_request' });
      }

      const request = {
        requestId,
        storeID,
        storeKey: storeID,
        requestType: body.requestType,
        saleId: Number(body.saleId),
        saleItemId: body.saleItemId == null ? null : Number(body.saleItemId),
        reason: String(body.reason || '').slice(0, 250),
        requestedAmount: body.requestedAmount == null ? null : Number(body.requestedAmount),
        discountPercent: body.discountPercent == null ? null : Number(body.discountPercent),
        employeeId: Number(body.employeeId),
        status: 'PENDING'
      };
      if (!request.reason || (request.requestType === 'SPECIAL_DISCOUNT'
          && !(request.discountPercent > 0 && request.discountPercent <= 100))) {
        return res.status(400).json({ error: 'invalid_approval_details' });
      }

      const db = admin.firestore();
      const reference = db.collection('posApprovals').doc(request.requestId);
      const existingStatus = await db.runTransaction(async (transaction) => {
        const current = await transaction.get(reference);
        if (current.exists) {
          if (current.get('storeID') !== request.storeID
              || current.get('requestType') !== request.requestType
              || current.get('saleId') !== request.saleId
              || current.get('employeeId') !== request.employeeId) return 'CONFLICT';
          return current.get('status') || 'PENDING';
        }
        transaction.create(reference, {
          ...request,
          createdAt: admin.firestore.FieldValue.serverTimestamp(),
          notificationDeliveryPending: true
        });
        return 'PENDING';
      });
      if (existingStatus === 'CONFLICT') return res.status(409).json({ error: 'approval_id_conflict' });
      if (existingStatus !== 'PENDING') {
        return res.status(200).json({ requestId, status: existingStatus, recipients: 0 });
      }

      const devices = await db.collection('posNotificationDevices')
        .where('storeKey', '==', request.storeKey)
        .where('role', 'in', ['GERENTE', 'DONO', 'MANAGER', 'OWNER'])
        .get();
      const recipients = await sendApprovalToTokens(request, devices.docs);
      await reference.update({
        notificationDeliveryPending: recipients === 0,
        lastNotificationAttemptAt: admin.firestore.FieldValue.serverTimestamp()
      });
      return res.status(200).json({ requestId, status: 'PENDING', recipients });
    }

    if (body.operation === 'decisions') {
      const storeID = String(body.storeID || '');
      const requestIds = Array.isArray(body.requestIds)
        ? [...new Set(body.requestIds.map(String))].slice(0, 50) : [];
      if (!/^\d+$/.test(storeID) || requestIds.some((id) => !/^[0-9a-f-]{36}$/i.test(id))) {
        return res.status(400).json({ error: 'invalid_decision_query' });
      }
      const docs = await Promise.all(requestIds.map((id) =>
        admin.firestore().collection('posApprovals').doc(id).get()));
      const decisions = docs.filter((doc) => doc.exists
          && doc.get('storeKey') === storeID
          && ['APPROVED', 'REJECTED'].includes(doc.get('status')))
        .map((doc) => ({
          requestId: doc.id,
          status: doc.get('status'),
          decisionUid: doc.get('decisionUid'),
          observation: doc.get('decisionObservation') || null,
          decidedAt: doc.get('decisionAt').toDate().toISOString()
        }));
      return res.status(200).json({ decisions });
    }

    return res.status(400).json({ error: 'unsupported_operation' });
  }
);

exports.notifyManagerForPendingPosApprovals = onDocumentCreated(
  { document: 'posNotificationDevices/{deviceId}', region: 'southamerica-east1' },
  async (event) => {
    const device = event.data && event.data.data();
    if (!device || !device.fcmToken || !managerRoles.has(normalizeRole(device.role))) return null;
    const pending = await admin.firestore().collection('posApprovals')
      .where('storeKey', '==', String(device.storeKey))
      .where('status', '==', 'PENDING')
      .limit(50)
      .get();
    if (pending.empty) return null;
    await sendApprovalToTokens(pending.docs.map((doc) => doc.data()), [event.data]);
    return null;
  }
);

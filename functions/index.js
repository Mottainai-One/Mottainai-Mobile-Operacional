const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

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
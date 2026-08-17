const admin = require('firebase-admin');
const functions = require('firebase-functions');

admin.initializeApp();

/**
 * Define custom claims (storeId, role) no token do usuário após login/criação.
 * O backend deve popular a coleção 'users' com { storeId, role } antes.
 */
exports.setUserClaims = functions.auth.user().onCreate(async (user) => {
  try {
    const db = admin.firestore();
    const userDoc = await db.collection('users').doc(user.uid).get();

    if (!userDoc.exists) {
      console.log(`Usuário ${user.uid} não encontrado na coleção 'users'`);
      return null;
    }

    const data = userDoc.data();
    const customClaims = {
      storeId: data.storeId || '',
      role: data.role || ''
    };

    await admin.auth().setCustomUserClaims(user.uid, customClaims);
    console.log(`Custom claims definidos para ${user.uid}:`, customClaims);
    return null;
  } catch (error) {
    console.error('Erro ao definir custom claims:', error);
    return null;
  }
});

/**
 * Atualiza claims quando o documento do usuário é atualizado (ex: mudança de role/loja).
 */
exports.updateUserClaims = functions.firestore
  .document('users/{userId}')
  .onUpdate(async (change, context) => {
    const newData = change.after.data();
    const userId = context.params.userId;

    if (!newData.storeId || !newData.role) {
      console.log(`Dados incompletos para ${userId}`);
      return null;
    }

    try {
      await admin.auth().setCustomUserClaims(userId, {
        storeId: newData.storeId,
        role: newData.role
      });
      console.log(`Custom claims atualizados para ${userId}`);
    } catch (error) {
      console.error(`Erro ao atualizar claims de ${userId}:`, error);
    }
    return null;
  });
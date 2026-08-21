// One-shot script para BACKFILL de custom claims em usuários EXISTENTES.
// Usa a chave de serviço apontada por GOOGLE_APPLICATION_CREDENTIALS (mantida FORA do repositório).
//
// Uso:
//   export GOOGLE_APPLICATION_CREDENTIALS="/caminho/seguro/service-account.json"
//   npm --prefix functions run set-claims
//   unset GOOGLE_APPLICATION_CREDENTIALS
//
// Depois de rodar, os usuários devem SAIR e ENTRAR novamente no app para renovar o token.

const admin = require('firebase-admin');

if (!process.env.GOOGLE_APPLICATION_CREDENTIALS) {
  console.error('Defina GOOGLE_APPLICATION_CREDENTIALS apontando para a chave de serviço.');
  process.exit(1);
}

admin.initializeApp();

function normalizeRole(role) {
  if (!role) return '';
  return String(role).trim().toUpperCase();
}

function storeIdOf(data) {
  return (data && (data.storeID || data.storeId)) || '';
}

async function main() {
  const db = admin.firestore();
  const snap = await db.collection('users').get();

  if (snap.empty) {
    console.log('Nenhum usuário encontrado na coleção users.');
    return;
  }

  let ok = 0;
  let skipped = 0;

  for (const doc of snap.docs) {
    const d = doc.data();
    const storeID = storeIdOf(d);
    const claims = {
      // storeID é o nome canônico exigido pelas rules do Firestore
      storeID: storeID,
      // storeId mantido como fallback temporário de compatibilidade
      storeId: storeID,
      role: normalizeRole(d.role)
    };

    if (!claims.storeID || !claims.role) {
      console.log(`SKIP ${doc.id} — storeID/role incompletos:`, claims);
      skipped++;
      continue;
    }

    await admin.auth().setCustomUserClaims(doc.id, claims);
    console.log(`SET ${doc.id}:`, claims);
    ok++;
  }

  console.log(`\nConcluído: ${ok} claims definidos, ${skipped} pulados.`);
  console.log('PRÓXIMO PASSO: usuários precisam sair e entrar novamente no app (renovar o token).');
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error('Falha no script:', error);
    process.exit(1);
  });
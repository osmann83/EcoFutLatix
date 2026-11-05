const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_student_1";
const BOB_UID = "bob_student_2";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

// --- SEGURIDAD: CONTROL DE ACCESO Y AUTENTICACIÓN ---

test("Usuario no autenticado: no puede leer ni escribir en users", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      nickname: "EcoAlice",
      school: "Liceo Ejército Pichincha",
      birthDate: "2010-05-15",
      totalScore: 0,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Usuario autenticado: puede crear su propio perfil con datos válidos", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      nickname: "EcoAlice",
      school: "Liceo Ejército Pichincha",
      birthDate: "2010-05-15",
      totalScore: 100,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Usuario autenticado: no puede crear perfil para otro UID", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      nickname: "BobEco",
      school: "Liceo Ejército Pichincha",
      birthDate: "2010-05-15",
      totalScore: 100,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Usuario autenticado: no puede inyectar campos no permitidos (Anti-Update-Gap)", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      nickname: "EcoAlice",
      school: "Liceo Ejército Pichincha",
      birthDate: "2010-05-15",
      totalScore: 100,
      createdAt: new Date(),
      updatedAt: new Date(),
      isAdmin: true, // Campo no permitido
    })
  );
});

test("Usuario autenticado: puede registrar un puntaje de minijuego propio", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("scores").doc("score_alice_1").set({
      userId: ALICE_UID,
      nickname: "EcoAlice",
      school: "Liceo Ejército Pichincha",
      gameId: "basuraleza",
      score: 450,
      itemsRecycled: 15,
      accuracy: 95,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Usuario autenticado: puede consultar leaderboard de un minijuego", async () => {
  // Precargar puntaje mediante contexto de administrador
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("scores").doc("score_seed_1").set({
      userId: BOB_UID,
      nickname: "EcoBob",
      school: "Colegio Industrial",
      gameId: "basuraleza",
      score: 500,
      itemsRecycled: 20,
      accuracy: 90,
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("scores").where("gameId", "==", "basuraleza").get()
  );
});

test("Usuario autenticado: no puede modificar el puntaje de otro usuario", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("scores").doc("bob_score_locked").set({
      userId: BOB_UID,
      nickname: "EcoBob",
      school: "Colegio Industrial",
      gameId: "encestar",
      score: 300,
      itemsRecycled: 10,
      accuracy: 80,
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("scores").doc("bob_score_locked").update({
      score: 9999,
      updatedAt: new Date(),
    })
  );
});

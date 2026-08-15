/**
 * QuizKing Server-Side Administrative Functions & Custom Claims Controller
 * 
 * Secure backend endpoints for assigning Firebase Auth Custom Claims,
 * creating administrative accounts, and bootstrapping the first SUPER_ADMIN.
 * 
 * IMPORTANT:
 * This runs on Google Cloud Functions / Node.js Admin SDK environment.
 * Service account credentials and Admin SDK are NEVER exposed to the Android client.
 */

const functions = require('firebase-functions');
const admin = require('firebase-admin');

if (!admin.apps.length) {
    admin.initializeApp();
}

const auth = admin.auth();
const db = admin.firestore();

/**
 * Standard Permission Matrix
 */
const ROLE_PERMISSIONS = {
    SUPER_ADMIN: {
        questionManagement: true,
        ragManagement: true,
        analytics: true,
        userManagement: true,
        systemConfiguration: true,
        moderation: true,
        tournamentManagement: true
    },
    ADMIN: {
        questionManagement: true,
        ragManagement: true,
        analytics: true,
        userManagement: false,
        systemConfiguration: false,
        moderation: true,
        tournamentManagement: true
    },
    CONTENT_MANAGER: {
        questionManagement: true,
        ragManagement: true,
        analytics: false,
        userManagement: false,
        systemConfiguration: false,
        moderation: false,
        tournamentManagement: false
    },
    ANALYST: {
        questionManagement: false,
        ragManagement: false,
        analytics: true,
        userManagement: false,
        systemConfiguration: false,
        moderation: false,
        tournamentManagement: false
    },
    SUPPORT: {
        questionManagement: false,
        ragManagement: false,
        analytics: false,
        userManagement: false,
        systemConfiguration: false,
        moderation: true,
        tournamentManagement: false
    }
};

/**
 * Cloud Function: Assign Admin Custom Claims & Sync to adminUsers/{uid}
 * Gated strictly: Caller must have SUPER_ADMIN claim.
 */
exports.setAdminRole = functions.https.onCall(async (data, context) => {
    // 1. Verify Caller Identity & Authority
    if (!context.auth) {
        throw new functions.https.HttpsError('unauthenticated', 'Caller must be authenticated.');
    }

    const callerToken = context.auth.token;
    const isSuperAdmin = callerToken.super_admin === true || callerToken.role === 'SUPER_ADMIN';

    if (!isSuperAdmin) {
        throw new functions.https.HttpsError('permission-denied', 'Only SUPER_ADMIN can assign administrator roles.');
    }

    const { targetUid, targetEmail, role, customPermissions } = data;
    if (!targetUid || !role) {
        throw new functions.https.HttpsError('invalid-argument', 'targetUid and role are required.');
    }

    const roleName = role.toUpperCase();
    const permissions = customPermissions || ROLE_PERMISSIONS[roleName] || ROLE_PERMISSIONS.SUPPORT;

    // 2. Set Custom Claims on Firebase Authentication
    const claims = {
        admin: true,
        super_admin: roleName === 'SUPER_ADMIN',
        role: roleName,
        adminRole: roleName,
        permissions: permissions
    };

    await auth.setCustomUserClaims(targetUid, claims);

    // 3. Update Firestore Document adminUsers/{uid}
    const adminRef = db.collection('adminUsers').doc(targetUid);
    const doc = await adminRef.get();

    const now = Date.now();
    const adminData = {
        uid: targetUid,
        email: targetEmail || (doc.exists ? doc.data().email : ''),
        role: roleName,
        status: 'ACTIVE',
        permissions: permissions,
        updatedAt: now
    };

    if (!doc.exists) {
        adminData.createdAt = now;
        adminData.lastLoginAt = 0;
        adminData.createdBy = context.auth.uid;
        adminData.displayName = (targetEmail || '').split('@')[0] || 'Administrator';
    }

    await adminRef.set(adminData, { merge: true });

    // 4. Log Immutable Audit Record
    await db.collection('adminAuditLogs').add({
        id: require('crypto').randomUUID(),
        adminUid: context.auth.uid,
        adminEmail: callerToken.email || 'superadmin@internal',
        action: 'SET_ADMIN_ROLE',
        target: targetEmail || targetUid,
        timestamp: now,
        metadata: {
            assignedRole: roleName,
            targetUid: targetUid
        }
    });

    return { success: true, uid: targetUid, role: roleName };
});

/**
 * Bootstrap Script / Cloud Function: Initial Super Admin Provisioning
 * 
 * Usage:
 * Run once via Firebase CLI or Secure CLI environment:
 * node bootstrap_super_admin.js <admin_email> <admin_password> <display_name>
 */
exports.bootstrapSuperAdmin = async function(email, password, displayName = 'Super Admin') {
    try {
        console.log(`[BOOTSTRAP] Initiating Super Admin provisioning for ${email}...`);

        let userRecord;
        try {
            userRecord = await auth.getUserByEmail(email);
            console.log(`[BOOTSTRAP] Existing Firebase user found (UID: ${userRecord.uid})`);
        } catch (e) {
            if (e.code === 'auth/user-not-found') {
                userRecord = await auth.createUser({
                    email: email,
                    password: password,
                    displayName: displayName,
                    emailVerified: true
                });
                console.log(`[BOOTSTRAP] Created new Firebase user (UID: ${userRecord.uid})`);
            } else {
                throw e;
            }
        }

        // Set authoritative Super Admin Custom Claims
        await auth.setCustomUserClaims(userRecord.uid, {
            admin: true,
            super_admin: true,
            role: 'SUPER_ADMIN',
            adminRole: 'SUPER_ADMIN',
            permissions: ROLE_PERMISSIONS.SUPER_ADMIN
        });

        // Create or update Firestore admin record
        const now = Date.now();
        await db.collection('adminUsers').doc(userRecord.uid).set({
            uid: userRecord.uid,
            email: userRecord.email,
            displayName: displayName,
            role: 'SUPER_ADMIN',
            status: 'ACTIVE',
            permissions: ROLE_PERMISSIONS.SUPER_ADMIN,
            createdAt: now,
            updatedAt: now,
            lastLoginAt: 0,
            createdBy: 'SYSTEM_BOOTSTRAP_CLI'
        }, { merge: true });

        // Record Audit Log
        await db.collection('adminAuditLogs').add({
            id: require('crypto').randomUUID(),
            adminUid: userRecord.uid,
            adminEmail: email,
            action: 'BOOTSTRAP_SUPER_ADMIN',
            target: email,
            timestamp: now,
            metadata: {
                initiator: 'SYSTEM_BOOTSTRAP_CLI',
                version: '2.5'
            }
        });

        console.log(`[BOOTSTRAP] SUCCESS: ${email} is now provisioned with SUPER_ADMIN claims and Firestore record.`);
        return { success: true, uid: userRecord.uid };
    } catch (err) {
        console.error(`[BOOTSTRAP] FAILED:`, err);
        throw err;
    }
};

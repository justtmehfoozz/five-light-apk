// FiveLight Backend - Custom OTP system removed.
// Real Firebase Native Email Verification link flow is used on the client.
import * as admin from "firebase-admin";
if (admin.apps.length === 0) {
  admin.initializeApp();
}

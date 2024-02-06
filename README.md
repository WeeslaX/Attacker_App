# Attacker_App

Work in progress app. List of possible attacks and configurations will be listed below.

Insecure Target - https://github.com/WeeslaX/Insecure_Target

## Local Socket MITM Attack
Ensure the following settings are set with build.gradle:

<u>Attacker App  - Monitoring Flavour </u>

* `buildConfigField 'boolean', 'ENABLE_ATTACKER_SERVICE', 'true'`
* `buildConfigField 'boolean', 'LOCAL_SOCKET_MITM_ATTACK', 'true'`

<u> Secure App - Monitoring Flavour </u>

* `buildConfigField 'boolean', 'SEND_MESSAGE_VIA_LOCAL_SOCKET', 'true'`
* `buildConfigField 'int', 'LOCAL_SOCKET_PORT', '50000'`

Steps
1. Run Attacker App.
2. Accept all permissions on Attacker App.
3. Run Insecure App.
4. Select "Start and Monitor Local Socket Server".
5. Note that no logs are generated (Server not initialised).
6. Run Secure App.
7. Accept all permissions.
8. Select "General Purpose Testing" button.
9. Verify on Attack App's logcat that message from Secure app has been received.


Note: Force stop Attacker App for Insecure App to resume use of socket in port 50000
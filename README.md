# Attacker_App

Work in progress app. List of possible attacks and configurations will be listed below.

Insecure Target - https://github.com/WeeslaX/Insecure_Target

## StrandHogg v2 Attack
Applicable only on Android 9 devices and older.
* Highly Targeted attack, requires target activity to be exported.

<u>Attacker App  - Monitoring Flavour </u>

* `buildConfigField 'boolean', 'STRANDHOGG_TWO_ATTACK', 'true'`
* `buildConfigField 'String', 'TARGET_PACKAGE_NAME', '\"sg.insecure.insecuretarget\"'`
* `buildConfigField 'String', 'TARGET_ACTIVITY', '\"sg.insecure.insecuretarget.StrandhoggVulActivity\"'`
* `buildConfigField 'boolean', 'CLOSE_APP', 'false'`

Steps
1. Install Attacker App and Insecure Target App.
2. Run Attacker App.
3. Ensure that Distraction Activity is displayed.
4. Minimise Attacker App.
5. Run Insecure Target App.
6. Verify that Malicious Activity is displayed instead. (Activity Hijack successful)

## Dynamic Code Injection Attack
Utilizes Create Package Context vulnerability on Insecure Target app to perform arbituary code injection.

`initialize` method from `com.attacker.app.utils.CodeToLoad` class is injected.

<u>Insecure Target App</u>
* buildConfigField 'String', 'TARGET_PACKAGE_PREFIX_FOR_DCL', "\"com.attacker\""

Steps
1. Run Attacker App and accept all permissions
2. Run Insecure Target App
3. Select "Create Package Context Scanning" button
4. Verify that Toast Message of successful injection is displayed

## Intent Redirection Attack via exploiting grantUriPermissions="true"
Exploiting Insecure Target App's misconfigured content provider to access secret_data.txt

<u>Insecure Target App</u>
* buildConfigField 'boolean', 'TEST_TEMPORARY_DB', 'true' - Note this will close the app for exploit to work. Disable if not testing exploit.

<u>Attacker App  - Monitoring Flavour </u>
* buildConfigField 'boolean', 'INSECURE_TARGET_GRANT_URI_PERMISSIONS_EXPLOIT', 'true'
* buildConfigField 'boolean', 'CLOSE_APP', 'false'

Steps:
1. Install both apps.
2. Run Attacker App.
3. Verify that secret_data.txt content is displayed on Logcat.

## OpenCloud Content Provider Exploit
Exploit exposed opencloud's content provider via SQL and Blind SQL Injection

<u>Attacker App  - Monitoring Flavour </u>
* buildConfigField 'boolean', 'OPENCLOUD_FILELIST_EXPLOIT', 'false'
* buildConfigField 'boolean', 'OPENCLOUD_FILELIST_EXPLOUT_POC', 'true'
* buildConfigField 'boolean', 'CLOSE_APP', 'false'

<u>OpenCloud App</u>
* App Version 2.21.1
* Requires OpenCloud running in a Docker image (Follow POC Instructions)
* Currently POC only works on Android Emulator (*shrug*)
* IMPORTANT: Before running Blind SQL Exploit, need to share any file first.

Steps:
1. Install both apps.
2. Ensure OpenCloud is setup properly.
3. Login with admin/admin
4. Ensure that files are pulled from the "cloud" on all folders
5. Share any folder. (Hold > select share > Public links > Save)
6. Run Attacker App
7. Free-play with UI.

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
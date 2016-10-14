For sign-in:

1. Create a new signing config (File->Project Structure->Signing) using the keystore provided (auggraffiti.jks).
2. Change the 'Signing Config' option in Build Types in File->Project Structure to this config. 
3. Sync project.
4. Go to build.gradle(Module:app). Change android->config->storeFile file to the appropriate auggraffiti.jks location on local system.
5. Do Build->Make Project
6. This will allow you to sign-in.


Permissions needed:
Settings->Apps->AugGraffiti->Permissions
Enable Location, Storage, Camera 


Augraffiti.apk which is the signed release apk is also included in the README folder. Please copy into it your device and install it to run the application.



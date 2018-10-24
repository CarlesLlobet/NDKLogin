#include <jni.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <android/log.h>
#include <zconf.h>
#include <sys/ptrace.h>
#include <wait.h>

#include "sqlite3.h"
#include "aes.h"
#include "b64.h"

#define DB_FILE "/data/data/com.example.cllobet.ndklogin/database.db"
sqlite3 *db;
char *err_msg = 0;

#define CBC 1
#define CTR 0
#define ECB 0

#define KEY_LEN 16

int rootCheck() {

}

int setupDatabase() {
    sqlite3_open_v2(DB_FILE, &db, SQLITE_OPEN_READWRITE | SQLITE_OPEN_CREATE, NULL);

    if (db == NULL) {
        __android_log_write(ANDROID_LOG_ERROR, "NATIVE_DATABASE", "Error opening database");
        return 1;
    }

    char *sql = "CREATE TABLE IF NOT EXISTS users (userName TEXT UNIQUE PRIMARY KEY, password TEXT NOT NULL);";
    int rc = sqlite3_exec(db, sql, 0, 0, &err_msg);

    if (rc != SQLITE_OK) {
        __android_log_write(ANDROID_LOG_ERROR, "NATIVE_DATABASE", "Error setting up database");
        sqlite3_free(err_msg);
        sqlite3_close(db);
        return 1;
    }

    return 0;
}

JNIEXPORT jboolean JNICALL
Java_com_example_cllobet_ndklogin_Domain_MainActivity_isDebuggingNative(JNIEnv *env,
                                                                        jobject instance) {
    jboolean ret = JNI_FALSE;
    int child_pid = fork();
    int status;
    if (child_pid == 0) {
        int parentPid = getppid();
        // attach parent process
//        if(ptrace(PTRACE_ATTACH,parentPid,NULL,NULL)==0){
//            // ptrace(PTRACE_DETACH, parentPid, NULL, NULL);
        _exit(0);
//        } else{
//            _exit(-1);
//        }
    } else {
        if (waitpid(child_pid, &status, 0) == -1) {
            ret = JNI_TRUE;
        }
        if (WIFEXITED(status)) {
            ret = JNI_TRUE;
        }
        kill(child_pid, SIGKILL);
    }
    if (!ret) {
        __android_log_write(ANDROID_LOG_ERROR, "Debugging: ", "PTrace worked");
    }
    return ret;
}

JNIEXPORT jboolean JNICALL
Java_com_example_cllobet_ndklogin_Domain_MainActivity_isDeviceRootedNative(JNIEnv *env,
                                                                           jobject instance) {
    //Check if we can touch a file in any of the following directories:
//        /
//        /data
//        /system
//        /system/bin
//        /system/sbin
//        /system/xbin
//        /vendor/bin
//        /sys
//        /sbin
//        /etc
//        /proc
//        /dev

    FILE *file = fopen("/testRoot.txt", "w+");
    if (file != NULL) {
        fputs("I AM ROOT!\n", file);
        fflush(file);
        fclose(file);
        __android_log_write(ANDROID_LOG_ERROR, "RootDetection: ", "/ writeable");
        return JNI_TRUE;
    }
    file = fopen("/data/testRoot.txt", "w+");
    if (file != NULL) {
        fputs("I AM ROOT!\n", file);
        fflush(file);
        fclose(file);
        __android_log_write(ANDROID_LOG_ERROR, "RootDetection: ", "/data writeable");
        return JNI_TRUE;
    }
    file = fopen("/system/testRoot.txt", "w+");
    if (file != NULL) {
        fputs("I AM ROOT!\n", file);
        fflush(file);
        fclose(file);
        __android_log_write(ANDROID_LOG_ERROR, "RootDetection: ", "/system writeable");
        return JNI_TRUE;
    }
    file = fopen("/system/bin/testRoot.txt", "w+");
    if (file != NULL) {
        fputs("I AM ROOT!\n", file);
        fflush(file);
        fclose(file);
        __android_log_write(ANDROID_LOG_ERROR, "RootDetection: ", "/system/bin writeable");
        return JNI_TRUE;
    }
    file = fopen("/system/sbin/testRoot.txt", "w+");
    if (file != NULL) {
        fputs("I AM ROOT!\n", file);
        fflush(file);
        fclose(file);
        __android_log_write(ANDROID_LOG_ERROR, "RootDetection: ", "/system/sbin writeable");
        return JNI_TRUE;
    }
    file = fopen("/system/xbin/testRoot.txt", "w+");
    if (file != NULL) {
        fputs("I AM ROOT!\n", file);
        fflush(file);
        fclose(file);
        __android_log_write(ANDROID_LOG_ERROR, "RootDetection: ", "/system/xbin writeable");
        return JNI_TRUE;
    }
    file = fopen("/vendor/bin/testRoot.txt", "w+");
    if (file != NULL) {
        fputs("I AM ROOT!\n", file);
        fflush(file);
        fclose(file);
        __android_log_write(ANDROID_LOG_ERROR, "RootDetection: ", "/vendor/bin writeable");
        return JNI_TRUE;
    }
    file = fopen("/sys/testRoot.txt", "w+");
    if (file != NULL) {
        fputs("I AM ROOT!\n", file);
        fflush(file);
        fclose(file);
        __android_log_write(ANDROID_LOG_ERROR, "RootDetection: ", "/sys writeable");
        return JNI_TRUE;
    }
    file = fopen("/sbin/testRoot.txt", "w+");
    if (file != NULL) {
        fputs("I AM ROOT!\n", file);
        fflush(file);
        fclose(file);
        __android_log_write(ANDROID_LOG_ERROR, "RootDetection: ", "/sbin writeable");
        return JNI_TRUE;
    }
    file = fopen("/etc/testRoot.txt", "w+");
    if (file != NULL) {
        fputs("I AM ROOT!\n", file);
        fflush(file);
        fclose(file);
        __android_log_write(ANDROID_LOG_ERROR, "RootDetection: ", "/etc writeable");
        return JNI_TRUE;
    }
    file = fopen("/proc/testRoot.txt", "w+");
    if (file != NULL) {
        fputs("I AM ROOT!\n", file);
        fflush(file);
        fclose(file);
        __android_log_write(ANDROID_LOG_ERROR, "RootDetection: ", "/proc writeable");
        return JNI_TRUE;
    }
    file = fopen("/dev/testRoot.txt", "w+");
    if (file != NULL) {
        fputs("I AM ROOT!\n", file);
        fflush(file);
        fclose(file);
        __android_log_write(ANDROID_LOG_ERROR, "RootDetection: ", "/dev writeable");
        return JNI_TRUE;
    }

    //Check if Superuser.apk exists
    file = fopen("/system/app/Superuser.apk", "r");
    if (file != NULL) {
        fclose(file);
        __android_log_write(ANDROID_LOG_ERROR, "RootDetection: ", "Superuser.apk found");
        return JNI_TRUE;
    }

    //Checks if the OTA certs exist, if not probably a custom ROM has been installed. They could be there and the phone rooted anyway
    file = fopen("/etc/security/otacerts.zip", "r");
    if (file != NULL) {
        fclose(file);
        __android_log_write(ANDROID_LOG_ERROR, "RootDetection: ",
                            "/etc/security/otacerts.zip exists");
        return JNI_TRUE;
    }

    return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com_example_cllobet_ndklogin_UI_LoginActivity_comparePass(JNIEnv *env, jobject instance,
                                                               jstring pass1, jstring pass2) {
    const char *nativePass1 = (*env)->GetStringUTFChars(env, pass1, 0);
    const char *nativePass2 = (*env)->GetStringUTFChars(env, pass2, 0);
    __android_log_write(ANDROID_LOG_ERROR, "NDK Pass 1: ", nativePass1);
    __android_log_write(ANDROID_LOG_ERROR, "NDK Pass 2: ", nativePass2);

    jboolean res = JNI_FALSE;
    if (strcmp(nativePass1, nativePass2) == 0) {
        __android_log_write(ANDROID_LOG_ERROR, "NDK Compare: ", "Estoy dentro");
        res = JNI_TRUE;
    }

    (*env)->ReleaseStringUTFChars(env, pass1, nativePass1);
    (*env)->ReleaseStringUTFChars(env, pass2, nativePass2);
    return res;
}

JNIEXPORT jboolean JNICALL
Java_com_example_cllobet_ndklogin_DB_DatabaseHandler_createDB(JNIEnv *env, jobject instance) {
    /* Open database */
    if (setupDatabase() != 0) {
        return JNI_FALSE;
    }
}

JNIEXPORT jboolean JNICALL
Java_com_example_cllobet_ndklogin_DB_DatabaseHandler_dropDB(JNIEnv *env, jobject instance) {
    /* Open database */
    if (setupDatabase() != 0) {
        return JNI_FALSE;
    }

    /* Create SQL statement */
    const char *dropQuery =
            "DROP TABLE IF EXISTS users;";

    /* Execute SQL statement */
    int rc = sqlite3_exec(db, dropQuery, 0, 0, &err_msg);
    if (rc != SQLITE_OK) {
        sqlite3_free(err_msg);
        return JNI_FALSE;
    }
    sqlite3_close(db);
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL
Java_com_example_cllobet_ndklogin_Domain_MainActivity_initPasscode(JNIEnv *env, jobject instance,
                                                                   jstring user, jstring key) {

    const char *password = "{=g|(u"; //r4nd0m (First 3 xored with 0x09 and last 3 with 0x18)

    const char *nativeUser = (*env)->GetStringUTFChars(env, user, 0);
    const char *nativeKey = (*env)->GetStringUTFChars(env, key, 0);
    addUserDB(nativeUser, password, nativeKey);
    (*env)->ReleaseStringUTFChars(env, user, nativeUser);
    (*env)->ReleaseStringUTFChars(env, key, nativeKey);
    return JNI_TRUE;
}

int addUserDB(const char *nativeUser, const char *nativePassword, const char *nativeKey) {
    if (setupDatabase() != 0) {
        return -1;
    }

    sqlite3_stmt *res;

    //xifrar
    uint8_t *cipherKey = (uint8_t *) calloc(64, sizeof(uint8_t));
    strcpy((char *) cipherKey, nativeKey);

    uint8_t *cipherText = (uint8_t *) calloc(64, sizeof(uint8_t));
    strcpy((char *) cipherText, nativePassword);

    char auxIV[64];

    //calcular IV
    for (int i = 63; i >= 0; i--) {
        auxIV[i] = nativePassword[i % strlen(nativePassword)];
    }

    uint8_t *iv = (uint8_t *) calloc(64, sizeof(uint8_t));
    strcpy((char *) iv, auxIV);

    struct AES_ctx ctx;

    AES_init_ctx_iv(&ctx, cipherKey, iv);
    AES_CBC_encrypt_buffer(&ctx, cipherText, 64);

    char *cipheredPassword = b64_encode(cipherText, 64);

    __android_log_write(ANDROID_LOG_ERROR, "NDK addUserDB: cipheredPass", cipheredPassword);

    /* Create SQL statement */
    char *insertQuery = "INSERT INTO users VALUES (?,?);";
    /*char insertQuery[50];
    strcpy(insertQuery,"INSERT INTO users VALUES (");
    strcat(insertQuery,nativeUser);
    strcat(insertQuery,",");
    strcat(insertQuery,nativePassword);
    strcat(insertQuery,");\0");*/
    //puts(insertQuery);

    /* Execute SQL statement */
    int rc = sqlite3_prepare_v2(db, insertQuery, -1, &res, 0);
    if (rc == SQLITE_OK) {
        sqlite3_bind_text(res, 1, nativeUser, strlen(nativeUser), 0);
        sqlite3_bind_text(res, 2, cipheredPassword, strlen(cipheredPassword), 0);

        sqlite3_step(res);
        __android_log_write(ANDROID_LOG_ERROR, "NDK addUserDB", "SQLITE_OK");
    } else {
        __android_log_write(ANDROID_LOG_ERROR, "NDK addUserDB", "NOT SQLITE_OK");

        __android_log_print(ANDROID_LOG_ERROR, "NDK addUserDB rc", "%d", rc);
        sqlite3_finalize(res);
        sqlite3_close(db);
        return -1;
    }
    /*rc = sqlite3_exec(db, insertQuery, callback, 0, &err_msg);
    if (rc != SQLITE_OK) {
        __android_log_write(ANDROID_LOG_ERROR, "NDK addUserDB", "NOT SQLITE_OK");
        sqlite3_free(err_msg);
        return JNI_FALSE;
    }*/

    sqlite3_finalize(res);
    sqlite3_close(db);

    return 0;
}

JNIEXPORT jstring JNICALL
Java_com_example_cllobet_ndklogin_UI_LoginActivity_xor(JNIEnv *env, jobject instance, jstring s) {
    const char *nativeS = (*env)->GetStringUTFChars(env, s, 0);
    uint8_t res[6];
    for (int i = 0; i < 6; i++) {
        if (i < 3) {
            res[i] = (uint8_t)((int)nativeS[i] ^ 9);
        } else {
            res[i] = (uint8_t)((int)nativeS[i] ^ 24);
        }
    }
    (*env)->ReleaseStringUTFChars(env, s, nativeS);
    jstring ret = (*env)->NewStringUTF(env, res);
    return ret;
}

JNIEXPORT jint JNICALL
Java_com_example_cllobet_ndklogin_UI_LoginActivity_signInDB(JNIEnv *env, jobject instance,
                                                            jstring user, jstring password,
                                                            jstring key) {
    if (setupDatabase() != 0) {
        return 1; //no working
    }

    sqlite3_stmt *res;

    const char *nativeUser = (*env)->GetStringUTFChars(env, user, 0);
    const char *nativePassword = (*env)->GetStringUTFChars(env, password, 0);
    const char *nativeKey = (*env)->GetStringUTFChars(env, key, 0);

    /* Create SQL statement */
    char *selectQuery = "SELECT password FROM users WHERE userName=?";

    /* Execute SQL statement */
    int rc = sqlite3_prepare_v2(db, selectQuery, -1, &res, 0);
    if (rc != SQLITE_OK) {
        __android_log_write(ANDROID_LOG_ERROR, "NDK signInDB", "NOT SQLITE_OK");
        sqlite3_finalize(res);
        sqlite3_close(db);
        return 1; //no working
    }

    sqlite3_bind_text(res, 1, nativeUser, strlen(nativeUser), 0);
    rc = sqlite3_step(res);
    if (rc != SQLITE_ROW) {
        __android_log_write(ANDROID_LOG_ERROR, "NDK signInDB", "NOT SQLITE_ROW");
        sqlite3_finalize(res);
        sqlite3_close(db);
        return 2; //no existe usuario
    }

    __android_log_write(ANDROID_LOG_ERROR, "NDK signInDB", "SQLITE_OK and SQLITE_ROW");
//    const char *password = reinterpret_cast<const char *>(sqlite3_column_text(res, 2));
    const char *pass = (char *) sqlite3_column_text(res, 0);

    __android_log_write(ANDROID_LOG_ERROR, "NDK signInDB: recoveredPass", pass);


    //XIFRAR PASS
    uint8_t *cipherKey = (uint8_t *) calloc(64, sizeof(uint8_t));
    strcpy((char *) cipherKey, nativeKey);

    uint8_t *cipherText = (uint8_t *) calloc(64, sizeof(uint8_t));
    strcpy((char *) cipherText, nativePassword);

    char auxIV[64];

    for (int i = 63; i >= 0; i--) {
        auxIV[i] = nativePassword[i % strlen(nativePassword)];
    }

    uint8_t *iv = (uint8_t *) calloc(64, sizeof(uint8_t));
    strcpy((char *) iv, auxIV);

    struct AES_ctx ctx;

    AES_init_ctx_iv(&ctx, cipherKey, iv);
    AES_CBC_encrypt_buffer(&ctx, cipherText, 64);

    const char *cipheredPassword = b64_encode(cipherText, 64);

    __android_log_write(ANDROID_LOG_ERROR, "NDK signInDB: cipheredPass", cipheredPassword);

    //FIN de xifrar

    if (strcmp(cipheredPassword, pass) == 0) {
        (*env)->ReleaseStringUTFChars(env, user, nativeUser);
        (*env)->ReleaseStringUTFChars(env, key, nativeKey);
        sqlite3_finalize(res);
        sqlite3_close(db);
        return 0; //existe usuario i passwd concuerda
    }

    (*env)->ReleaseStringUTFChars(env, user, nativeUser);
    (*env)->ReleaseStringUTFChars(env, key, nativeKey);
    sqlite3_finalize(res);
    sqlite3_close(db);
    return 3; //existe usuario pero password no concuerda
}
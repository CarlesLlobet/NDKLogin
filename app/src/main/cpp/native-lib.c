#include <jni.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <android/log.h>

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
Java_com_example_cllobet_ndklogin_UI_LoginActivity_addUserDB(JNIEnv *env, jobject instance,
                                                               jstring user, jstring password, jstring key) {
    if (setupDatabase() != 0) {
        return JNI_FALSE;
    }

    sqlite3_stmt *res;
    const char *nativeUser = (*env)->GetStringUTFChars(env, user, 0);
    const char *nativePassword = (*env)->GetStringUTFChars(env, password, 0);
    const char *nativeKey = (*env)->GetStringUTFChars(env, key, 0);

    //xifrar
    uint8_t *cipherKey = (uint8_t*)calloc(64, sizeof(uint8_t));
    strcpy((char*)cipherKey, nativeKey);

    uint8_t *cipherText = (uint8_t*)calloc(64, sizeof(uint8_t));
    strcpy((char*)cipherText, nativePassword);

    char auxIV[64];

    //calcular IV
    for(int i=63; i>=0; i-- ) {
        auxIV[i] = nativePassword[i%strlen(nativePassword)];
    }

    uint8_t *iv = (uint8_t*)calloc(64, sizeof(uint8_t));
    strcpy((char*)iv, auxIV);

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
        return JNI_FALSE;
    }
    /*rc = sqlite3_exec(db, insertQuery, callback, 0, &err_msg);
    if (rc != SQLITE_OK) {
        __android_log_write(ANDROID_LOG_ERROR, "NDK addUserDB", "NOT SQLITE_OK");
        sqlite3_free(err_msg);
        return JNI_FALSE;
    }*/

    sqlite3_finalize(res);
    sqlite3_close(db);
    (*env)->ReleaseStringUTFChars(env, user, nativeUser);
    (*env)->ReleaseStringUTFChars(env, password, nativePassword);
    (*env)->ReleaseStringUTFChars(env, key, nativeKey);
    return JNI_TRUE;
}

JNIEXPORT jint JNICALL
Java_com_example_cllobet_ndklogin_UI_LoginActivity_signInDB(JNIEnv *env, jobject instance,
                                                              jstring user, jstring password, jstring key) {
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
    const char *pass = (char *)sqlite3_column_text(res, 0);

    __android_log_write(ANDROID_LOG_ERROR, "NDK signInDB: recoveredPass", pass);


    //XIFRAR PASS
    uint8_t *cipherKey = (uint8_t*)calloc(64, sizeof(uint8_t));
    strcpy((char*)cipherKey, nativeKey);

    uint8_t *cipherText = (uint8_t*)calloc(64, sizeof(uint8_t));
    strcpy((char*)cipherText, nativePassword);

    char auxIV[64];

    for(int i=63; i>=0; i-- ) {
        auxIV[i] = nativePassword[i%strlen(nativePassword)];
    }

    uint8_t *iv = (uint8_t*)calloc(64, sizeof(uint8_t));
    strcpy((char*)iv, auxIV);

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
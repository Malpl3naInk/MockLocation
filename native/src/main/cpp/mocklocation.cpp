#include <jni.h>
#include <string>
#include <vector>
#include <cstring>

/**
 * 真正的 native 实现
 * 名字随便起，和 Java 完全无关
 */
static jstring hello_impl(JNIEnv* env, jobject thiz) {
    std::string msg = "Hello from JNI (Compose)";
    return env->NewStringUTF(msg.c_str());
}

// ======================= // 工具：Class → JNI 类型签名 // =======================
static std::string jclassToJNISig(JNIEnv* env, jclass cls) {
    //primitive types
    if (env->IsSameObject(cls, env->FindClass("java/lang/Void")))       return "V";
    if (env->IsSameObject(cls, env->FindClass("java/lang/Boolean")))    return "Z";
    if (env->IsSameObject(cls, env->FindClass("java/lang/Byte")))       return "B";
    if (env->IsSameObject(cls, env->FindClass("java/lang/Character")))  return "C";
    if (env->IsSameObject(cls, env->FindClass("java/lang/Short")))      return "S";
    if (env->IsSameObject(cls, env->FindClass("java/lang/Integer")))    return "I";
    if (env->IsSameObject(cls, env->FindClass("java/lang/Long")))       return "J";
    if (env->IsSameObject(cls, env->FindClass("java/lang/Float")))      return "F";
    if (env->IsSameObject(cls, env->FindClass("java/lang/Double")))     return "D";
    // array?
    jclass classClass = env->FindClass("java/lang/Class");
    jmethodID isArray = env->GetMethodID(classClass, "isArray", "()Z");
    if (env->CallBooleanMethod(cls, isArray)) {
        jmethodID getComponent = env->GetMethodID(
                classClass,
                "getComponentType",
                "()Ljava/lang/Class;");
        jclass comp = (jclass)env->CallObjectMethod(cls, getComponent);
        return "[" + jclassToJNISig(env, comp);
    }
    // object
    jmethodID getName = env->GetMethodID(classClass, "getName", "()Ljava/lang/String;");
    jstring name = (jstring)env->CallObjectMethod(cls, getName);
    const char* utf = env->GetStringUTFChars(name, nullptr);
    std::string sig = "L";
    for (const char* p = utf; *p; ++p) {
        sig += (*p == '.') ? '/' : *p;
    }
    sig += ";";
    env->ReleaseStringUTFChars(name, utf); return sig;
}
// ======================= // Method → JNI 签名 // =======================
static std::string buildMethodSig(JNIEnv* env, jobject method) {
    jclass methodCls = env->FindClass("java/lang/reflect/Method");
    jmethodID getParams = env->GetMethodID(methodCls, "getParameterTypes", "()[Ljava/lang/Class;");
    jmethodID getRet = env->GetMethodID(methodCls, "getReturnType", "()Ljava/lang/Class;");
    jobjectArray params = (jobjectArray)env->CallObjectMethod(method, getParams);
    jclass ret = (jclass)env->CallObjectMethod(method, getRet);
    std::string sig = "(";
    jsize count = env->GetArrayLength(params);
    for (jsize i = 0; i < count; ++i) {
        jclass p = (jclass)env->GetObjectArrayElement(params, i); sig += jclassToJNISig(env, p);
    }
    sig += ")";
    sig += jclassToJNISig(env, ret);
    return sig;
}

/** * so 被加载时调用 */
JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM* vm, void*) {
    JNIEnv* env = nullptr;

    if (vm->GetEnv((void**)&env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    jclass cls = env->FindClass("ink/moling/mocklocation/nativelib/NativeLib");
    if (!cls) return JNI_ERR;

    // ===== Class.getDeclaredMethods =====
    jclass classCls = env->FindClass("java/lang/Class");
    jmethodID getDeclaredMethods =
            env->GetMethodID(
                    classCls,
                    "getDeclaredMethods",
                    "()[Ljava/lang/reflect/Method;"
            );

    jobjectArray methods =
            (jobjectArray)env->CallObjectMethod(cls, getDeclaredMethods);

    // ===== Method APIs =====
    jclass methodCls = env->FindClass("java/lang/reflect/Method");
    jmethodID getName =
            env->GetMethodID(methodCls, "getName", "()Ljava/lang/String;");
    jmethodID getModifiers =
            env->GetMethodID(methodCls, "getModifiers", "()I");

    // ===== Modifier.isNative =====
    jclass modifierCls = env->FindClass("java/lang/reflect/Modifier");
    jmethodID isNative =
            env->GetStaticMethodID(modifierCls, "isNative", "(I)Z");

    std::vector<JNINativeMethod> natives;

    jsize count = env->GetArrayLength(methods);
    for (jsize i = 0; i < count; ++i) {
        jobject m = env->GetObjectArrayElement(methods, i);

        jint mods = env->CallIntMethod(m, getModifiers);
        if (!env->CallStaticBooleanMethod(modifierCls, isNative, mods)) {
            continue; // 非 native，跳过
        }

        // 方法名（混淆后名字）
        jstring jname = (jstring)env->CallObjectMethod(m, getName);
        const char* name = env->GetStringUTFChars(jname, nullptr);

        // JNI 签名（你已经写好了）
        std::string sig = buildMethodSig(env, m);

        // ===== 绑定规则（核心）=====
        if (sig == "()Ljava/lang/String;") {
            JNINativeMethod nm {
                    name,
                    sig.c_str(),
                    (void*)hello_impl
            };
            natives.push_back(nm);
        }

        env->ReleaseStringUTFChars(jname, name);
    }

    if (!natives.empty()) {
        if (env->RegisterNatives(
                cls,
                natives.data(),
                natives.size()
        ) != 0) {
            return JNI_ERR;
        }
    }

    return JNI_VERSION_1_6;
}
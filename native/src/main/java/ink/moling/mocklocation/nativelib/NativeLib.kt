package ink.moling.mocklocation.nativelib

class NativeLib {

    /**
     * A native method that is implemented by the 'mocklocation' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'mocklocation' library on application startup.
        init {
            System.loadLibrary("mocklocation")
        }
    }
}
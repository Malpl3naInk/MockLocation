package ink.moling.mocklocation.utils

import ink.moling.mocklocation.R

class HelpMessages {

    companion object {
        private val presetHelpMessages = mapOf(
            "GPS Provider Error" to R.string.error_help_no_mock_location_app
        )
        fun isExists(type: String): Boolean {
            return presetHelpMessages.containsKey(type)
        }

        fun getHelpMessage(type: String): Int {
            return presetHelpMessages.getValue(type)
        }
    }
}
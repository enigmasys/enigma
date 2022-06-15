package common.services.auth

interface AuthService {
    fun getAuthToken(): String
    fun setAuthToken(authToken: String)
}


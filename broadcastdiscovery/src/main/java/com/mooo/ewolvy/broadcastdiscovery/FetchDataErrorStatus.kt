package com.mooo.ewolvy.broadcastdiscovery

enum class FetchDataErrorStatus{
    NO_ERROR,
    INVALID_SEND_DATA,
    INVALID_PORT,
    CLIENT_SOCKET_ERROR,
    INVALID_ACTIVITY,
    INVALID_TIMEOUT,
    INVALID_RESEND_TIME,
    INVALID_SERVICE,
    UNKNOWN_ERROR,
    NO_WIFI_CONNECTION_ERROR
}
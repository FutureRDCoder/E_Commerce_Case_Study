import apiClient from "../api/apiClient";

const AUTH_BASE_URL = "/api/auth";

export const login = async (loginData) => {
    const response = await apiClient.post(
        `${AUTH_BASE_URL}/login`,
        loginData
    );

    return response.data;
};

export const register = async (registerData) => {
    const response = await apiClient.post(
        `${AUTH_BASE_URL}/register`,
        registerData
    );

    return response.data;
};
import { defineStore } from 'pinia';
import { ref, watchEffect } from 'vue';
import axios from 'axios';

// axios 기본 설정
const api = axios.create({
    baseURL: 'http://localhost:8080'  // 백엔드 서버 주소
});

export const useDarkModeStore = defineStore('darkMode', () => {
    const isDarkMode = ref<boolean>(false);

    // axios 요청에 토큰 추가하는 헬퍼 함수
    const getAuthHeaders = () => {
        const token = localStorage.getItem('token');
        return {
            headers: {
                Authorization: `Bearer ${token}`
            }
        };
    };

    // 서버에서 다크 모드 상태 가져오기
    const fetchDarkModeStatus = async () => {
        try {
            const response = await api.get('/auth/dark', getAuthHeaders());
            isDarkMode.value = response.data.isDarkMode;
            localStorage.setItem('darkMode', String(isDarkMode.value));
        } catch (error) {
            console.error('다크 모드 상태를 가져오는 중 오류 발생:', error);
            // 에러 발생 시 로컬스토리지의 값을 사용
            const savedDarkMode = localStorage.getItem('darkMode');
            if (savedDarkMode !== null) {
                isDarkMode.value = savedDarkMode === 'true';
            }
        }
    };

    // 다크 모드 토글 기능
    const toggleDarkMode = async () => {
        const previousState = isDarkMode.value;
        
        try {
            const response = await api.put('/auth/dark', null, {
                ...getAuthHeaders(),
                params: { darkMode: !previousState }  // 현재 상태의 반대값을 전송
            });
            
            if (response.status === 200) {
                isDarkMode.value = !previousState;
                localStorage.setItem('darkMode', String(isDarkMode.value));
            }
        } catch (error: any) {  // error를 any 타입으로 지정
            console.error('다크 모드 변경 실패:', error);
            if (error.response?.status === 401) {
                // 인증 실패시 로그인 페이지로 리다이렉트
                window.location.href = '/login';
            }
            // 상태 복구
            isDarkMode.value = previousState;
            localStorage.setItem('darkMode', String(previousState));
        }
    };

    // 상태 변화 감지 및 자동 적용
    watchEffect(() => {
        if (isDarkMode.value) {
            document.documentElement.classList.add('dark-mode');
        } else {
            document.documentElement.classList.remove('dark-mode');
        }
    });

    return {
        isDarkMode,
        fetchDarkModeStatus,
        toggleDarkMode
    };
});

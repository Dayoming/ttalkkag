<template>구글 로그인 인증 중...</template>
<script>
export default {
    name: "GoogleLoginCallback",
    async mounted() {
        try {
            const code = this.$route.query.code;
            console.log(code);

            if (code) {
                const response = await this.$axios.get("/api/auth/oauth/google", {
                    params: { code },
                });

                const { accessToken, refreshToken, verified } = response.data;

                // localStorage에 데이터 저장
                localStorage.setItem("accessToken", accessToken);
                localStorage.setItem("refreshToken", refreshToken);

                // 프로젝트 생성 (최초 로그인인 경우만)
                if (!verified) {
                    const createProjectResponse = await this.$axios.post(
                        "/api/projects",
                        { name: "default" }
                    );
                    console.log("프로젝트 생성:", createProjectResponse);
                }
                // 모든 작업 완료 후 페이지 이동
                this.$router.push("/test-api").then(() => {
                    // 페이지 이동 완료 후 새로고침
                    window.location.reload();
                });
            } else {
                console.error("구글 인증 코드가 없습니다.");
                this.$router.push("/login");
            }
        } catch (error) {
            console.error("구글 인증 실패:", error);
            alert("구글 로그인 처리 중 문제가 발생했습니다.");
            this.$router.push("/login");
        }
    },
}
</script>

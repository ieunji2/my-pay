from locust import HttpUser, task, between


class UserA(HttpUser):  # HttpUser를 상속받아 사용자 클래스를 정의
    host = "http://localhost:8080"  # 요청을 보낼 호스트 URL 설정
    wait_time = between(1, 5)  # 각 요청 사이의 대기 시간 설정(1초에서 5초 사이의 랜덤한 시간)

    @task  # 부하테스트 동안 실행될 작업을 나타내는 데코레이터
    def get_wallet(self):
        response = self.client.get("/money/v1/money")
        print("UserA ::: find_account ::: response ::: ", response.text)

    def on_start(self):  # 사용자 인스턴스가 실행될 때 호출되는 함수
        self.client.headers = {"Authorization": "Bearer 123"}  # 요청 헤더에 인증 토큰 셋팅

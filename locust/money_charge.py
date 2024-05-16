from locust import HttpUser, task, between


class UserA(HttpUser):
    host = "http://localhost:8080"
    wait_time = between(1, 5)

    @task
    def charge_money(self):
        response = self.client.post("/money/v1/money/charge",
                                    json={"amount": 2000, "summary": "locust UserA"})
        print("UserA ::: charge_money ::: response ::: ", response.text)

    def on_start(self):
        self.client.headers = {"Authorization": "Bearer 123"}
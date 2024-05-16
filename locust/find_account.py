from locust import HttpUser, task


def ping(self):
    self.client.get("/money/health/ping")


class UserA(HttpUser):
    host = "http://localhost:8080"
    accountId = ""

    @task
    def find_account(self):
        response = self.client.get("/accounts/v1/accounts/" + str(self.accountId))
        print("UserA ::: find_account ::: response ::: ", response.text)

    def on_start(self):
        self.accountId = self.client.post("/accounts/v1/accounts",
                                          json={"name": "foo", "email": "foo@mypay.com"}).json()["id"]
        print("UserA ::: on_start ::: accountId ::: ", self.accountId)

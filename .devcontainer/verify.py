"""Exercise the same dev container used by Codespaces, with isolated test records."""

import os
from pathlib import Path
import subprocess
import sys
import unittest
import urllib.error
import urllib.parse
import urllib.request
import uuid

import lab


def require(condition, message):
    if not condition:
        raise AssertionError(message)
    print(f"PASS: {message}", flush=True)


def request(number, path="", form=None):
    data = urllib.parse.urlencode(form).encode("utf-8") if form is not None else None
    req = urllib.request.Request(f"http://127.0.0.1:8080/lab1-{number}/{path}", data=data)
    if data is not None:
        req.add_header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
    with urllib.request.urlopen(req, timeout=30) as response:
        require(response.status == 200, f"HTTP 200: 1.{number}/{path}")
        return response.read().decode("utf-8")


class NoRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        return None


def verify_proxy_redirect():
    opener = urllib.request.build_opener(NoRedirect)
    req = urllib.request.Request("http://127.0.0.1:8080/", headers={
        "Host": "codespaces-test.example", "X-Forwarded-Proto": "https",
        "X-Forwarded-Host": "codespaces-test.example", "X-Forwarded-Port": "443",
    })
    try:
        opener.open(req, timeout=10)
    except urllib.error.HTTPError as error:
        require(error.code == 302 and error.headers["Location"] ==
                "https://codespaces-test.example/lab1-9/", "HTTPS redirect through the Codespaces proxy")
    else:
        raise AssertionError("Expected redirect from the forwarded root URL.")


def verify_insert(number, restart=False):
    database = f"cityparks_lab{number}"
    token = uuid.uuid4().hex
    park_name = f"Проверочный парк {token}"
    city_name = f"Проверочный город {token}"
    park_id = city_id = None
    try:
        park = {"name": park_name, "type": "городской", "area": "12.75",
                "hasWater": "on", "buildDate": "2020-02-29"}
        require(park_name in request(number, "park", park), f"1.{number}: park INSERT and Cyrillic redirect")
        park_id = int(lab.psql(database, f"SELECT id FROM parks WHERE name='{park_name}'"))
        city = {"name": city_name, "status": "город", "population": "12345", "parkId": park_id}
        require(city_name in request(number, "city", city), f"1.{number}: linked city INSERT")
        city_id = int(lab.psql(database, f"SELECT id FROM cities WHERE name='{city_name}'"))
        if restart:
            lab.prepare_databases()
            lab.stop()
            lab.start()
            require(city_name in request(number, "city"), "Restart and repeated initialization preserve user data")
        if number == 15:
            require(city_name in request(number, f"editcity?id={city_id}"), "City edit form")
            require(park_name in request(number, f"editpark?id={park_id}"), "Park edit form")
            city.update(id=city_id, name=city_name + " изменен", population="54321")
            require(city["name"] in request(number, "editcity", city), "City UPDATE keeps UTF-8 text")
            park.update(id=park_id, area="33.50")
            request(number, "editpark", park)
            require(lab.psql(database, f"SELECT area FROM parks WHERE id={park_id}") == "33.50", "Park UPDATE")
            try:
                request(number, f"deletepark?id={park_id}")
            except urllib.error.HTTPError as error:
                require(error.code == 409, "Related park deletion is rejected")
            else:
                raise AssertionError("Related park was deleted.")
            request(number, f"deletecity?id={city_id}")
            request(number, f"deletepark?id={park_id}")
            require(lab.psql(database, f"SELECT count(*) FROM cities WHERE id={city_id}") == "0", "City DELETE")
            require(lab.psql(database, f"SELECT count(*) FROM parks WHERE id={park_id}") == "0", "Park DELETE")
    finally:
        if city_id is not None:
            lab.psql(database, f"DELETE FROM cities WHERE id={city_id}")
        if park_id is not None:
            lab.psql(database, f"DELETE FROM parks WHERE id={park_id}")


def main():
    if sys.platform != "linux" or os.environ.get("LAB_CONTAINER") != "1":
        raise RuntimeError("Integration verification runs only in the dev container.")
    suite = unittest.defaultTestLoader.discover(str(Path(__file__).parent), pattern="test_*.py")
    require(unittest.TextTestRunner(verbosity=2).run(suite).wasSuccessful(), "Launcher unit tests")
    for number in (1, 2):
        completed = subprocess.run([sys.executable, str(Path(lab.__file__)), str(number)],
                                   capture_output=True, text=True, encoding="utf-8", check=True)
        require("Ростов-на-Дону" in completed.stdout, f"1.{number}: console variant 12")
        lab.console(number, 0)
    lab.console(2, 12)
    lab.show_domain()
    lab.start()
    try:
        verify_proxy_redirect()
        for number in lab.WEB_NUMBERS:
            require("Города и парки" in request(number), f"1.{number}: home page and UTF-8")
            if number >= 10:
                for entity in ("city", "park"):
                    page = request(number, entity)
                    expected = "Привет" if number < 12 else "Парк имени Горького"
                    require(expected in page, f"1.{number}: {entity} model and JSP")
        for asset in ("js/jquery-3.6.4.js", "js/bootstrap.bundle.min.js", "js/bootstrap-check.js"):
            request(9, asset)
        before = {n: lab.psql(f"cityparks_lab{n}", "SELECT count(*) FROM cities") for n in range(13, 16)}
        verify_insert(14)
        verify_insert(15, restart=True)
        after = {n: lab.psql(f"cityparks_lab{n}", "SELECT count(*) FROM cities") for n in range(13, 16)}
        require(before == after, "Independent databases and cleanup of only test records")
        print("RESULT: ALL 9 CODESPACES LABS PASSED", flush=True)
    finally:
        lab.stop()


if __name__ == "__main__":
    try:
        main()
    except Exception:
        log = lab.TOMCAT / "logs/catalina.out"
        if log.exists():
            print("\nTomcat log:\n" + "\n".join(log.read_text(encoding="utf-8", errors="replace").splitlines()[-100:]))
        raise

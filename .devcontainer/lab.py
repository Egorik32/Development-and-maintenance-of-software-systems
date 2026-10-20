#!/usr/bin/env python3
"""Build the existing laboratory sources inside the Linux dev container."""

import argparse
import json
import os
from pathlib import Path
import shutil
import socket
import subprocess
import sys
import time
import urllib.error
import urllib.request
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
STATE = ROOT / ".codespaces"
TOMCAT = STATE / "tomcat"
FOLDERS = {
    1: "Lab1_AnonymousClasses_V12",
    2: "Lab2_LambdaExpressions_V12",
    9: "Lab9_JavaEE_MainPage_V12",
    10: "Lab10_JavaEE_ServletTemplates_V12",
    11: "Lab11_JavaEE_Domain_V12",
    12: "Lab12_JavaEE_Views_V12",
    13: "Lab13_JavaEE_Database_V12",
    14: "Lab14_JavaEE_Insert_V12",
    15: "Lab15_JavaEE_EditDelete_V12",
}
WEB_NUMBERS = tuple(range(9, 16))


def run(args, **kwargs):
    return subprocess.run([str(arg) for arg in args], check=True, **kwargs)


def lab_number(value):
    number = value.removeprefix("1.")
    if not number.isdigit() or int(number) not in FOLDERS:
        raise argparse.ArgumentTypeError("Supported labs: 1, 2, 9-15 (or 1.1, 1.2, 1.9-1.15).")
    return int(number)


def clean_build(path):
    if not path.resolve().is_relative_to(STATE.resolve()) or path.resolve() == STATE.resolve():
        raise ValueError("Build directory must be inside .codespaces.")
    if path.is_symlink():
        raise ValueError("Build directory must not be a symlink.")
    if path.exists():
        shutil.rmtree(path)
    path.mkdir(parents=True)


def psql(database, sql):
    return run(["psql", "-X", "-w", "-v", "ON_ERROR_STOP=1", "-tA", "-d", database,
                "-c", sql], capture_output=True, text=True, encoding="utf-8").stdout.strip()


def prepare_databases():
    deadline = time.monotonic() + 60
    while subprocess.run(["pg_isready", "-q"], check=False).returncode:
        if time.monotonic() > deadline:
            raise RuntimeError("PostgreSQL is not ready. Rebuild the Codespace container.")
        time.sleep(1)
    for number in range(13, 16):
        database = f"cityparks_lab{number}"
        if psql("postgres", f"SELECT 1 FROM pg_database WHERE datname='{database}'") != "1":
            run(["createdb", "-w", "--encoding=UTF8", "--template=template0", database])
        tables = psql(database, "SELECT count(*) FROM information_schema.tables "
                      "WHERE table_schema='public' AND table_name IN ('cities','parks')")
        if tables == "0":
            run(["psql", "-X", "-w", "-v", "ON_ERROR_STOP=1", "--single-transaction",
                 "-d", database, "-f", ROOT / FOLDERS[number] / "sql/schema.sql"])
        elif tables != "2":
            raise RuntimeError(f"{database}: incomplete schema; existing data was not changed.")
        print(f"Database {database}: ready; existing records preserved.", flush=True)


def configure_tomcat():
    home = Path(os.environ.get("CATALINA_HOME", "/usr/local/tomcat"))
    conf = TOMCAT / "conf"
    conf.mkdir(parents=True, exist_ok=True)
    for source in (home / "conf").iterdir():
        target = conf / source.name
        if source.is_file() and not target.exists():
            shutil.copy2(source, target)
    server_file = conf / "server.xml"
    server = ET.parse(server_file)
    server.getroot().set("port", "-1")
    connector = server.find("./Service/Connector[@protocol='HTTP/1.1']")
    if connector is None:
        raise RuntimeError("Tomcat HTTP connector not found.")
    connector.set("address", "127.0.0.1")
    connector.set("port", "8080")
    engine = server.find("./Service/Engine")
    valve_class = "org.apache.catalina.valves.RemoteIpValve"
    if engine.find(f"Valve[@className='{valve_class}']") is None:
        ET.SubElement(engine, "Valve", {
            "className": valve_class, "remoteIpHeader": "x-forwarded-for",
            "protocolHeader": "x-forwarded-proto", "hostHeader": "x-forwarded-host",
            "portHeader": "x-forwarded-port",
        })
    server.write(server_file, encoding="utf-8", xml_declaration=True)
    for name in ("logs", "temp", "work", "webapps"):
        (TOMCAT / name).mkdir(exist_ok=True)
    root_app = TOMCAT / "webapps/ROOT"
    root_app.mkdir(exist_ok=True)
    (root_app / "index.jsp").write_text(
        '<% response.sendRedirect(request.getContextPath() + "/lab1-9/"); %>\n', encoding="utf-8")


def build_web(number):
    folder = ROOT / FOLDERS[number]
    staging = STATE / "build" / str(number)
    clean_build(staging)
    web = folder / "src/main/webapp"
    shutil.copytree(web, staging, dirs_exist_ok=True)
    sources = sorted((folder / "src/main/java").rglob("*.java"))
    classes = staging / "WEB-INF/classes"
    classes.mkdir(parents=True, exist_ok=True)
    if sources:
        run(["javac", "--release", "17", "-encoding", "UTF-8", "-cp", web / "WEB-INF/lib/*",
             "-d", classes, *sources])
    for resource in (folder / "src/main/java").rglob("*.properties"):
        relative = resource.relative_to(folder / "src/main/java")
        target = classes / relative
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(resource, target)
    # Servlet classes are supplied by Tomcat, not by the application WAR.
    (staging / "WEB-INF/lib/jakarta.servlet-api-5.0.0.jar").unlink(missing_ok=True)
    pending = STATE / f"lab1-{number}.war"
    run(["jar", "--create", "--file", pending, "-C", staging, "."])
    (TOMCAT / "webapps").mkdir(parents=True, exist_ok=True)
    pending.replace(TOMCAT / f"webapps/lab1-{number}.war")
    print(f"Built laboratory 1.{number}.", flush=True)


def console(number, example=None):
    folder = ROOT / FOLDERS[number]
    if example is not None:
        manifest = json.loads((folder / "examples/examples.json").read_text(encoding="utf-8-sig"))
        selected = [item for item in manifest if example == 0 or item["number"] == example]
        if not selected:
            raise ValueError("This example number does not exist.")
        for item in selected:
            out = STATE / "examples" / str(number) / str(item["number"])
            clean_build(out)
            sources = sorted((folder / "examples" / item["folder"]).glob("*.java"))
            run(["javac", "--release", "8", "-encoding", "UTF-8", "-d", out, *sources])
            output = run(["java", "-Dfile.encoding=UTF-8", "-cp", out, item["class"]],
                         capture_output=True, text=True, encoding="utf-8").stdout
            if output.splitlines() != item["expected"]:
                raise RuntimeError(f"Unexpected output for example {item['number']}: {output}")
            print(f"Example {item['number']}: {item['folder']}\n{output}", end="", flush=True)
        print(f"RESULT: ALL {len(selected)} SELECTED EXAMPLES PASSED", flush=True)
        return
    out = STATE / "console" / str(number)
    clean_build(out)
    main = "Lab1AnonymousClassesV12" if number == 1 else "Lab2LambdaExpressionsV12"
    run(["javac", "--release", "8", "-encoding", "UTF-8", "-d", out, folder / f"src/{main}.java"])
    run(["java", "-Dfile.encoding=UTF-8", "-cp", out, main])


def show_domain():
    out = STATE / "domain"
    clean_build(out)
    sources = sorted((ROOT / FOLDERS[11] / "src/main/java/domain").glob("*.java"))
    run(["javac", "--release", "17", "-encoding", "UTF-8", "-d", out,
         *sources, ROOT / "tools/DomainCheck.java"])
    run(["java", "-Dfile.encoding=UTF-8", "-cp", out, "DomainCheck"])


def running():
    pid_file = TOMCAT / "tomcat.pid"
    try:
        pid = int(pid_file.read_text(encoding="ascii").strip())
        cmdline = Path(f"/proc/{pid}/cmdline").read_bytes().split(b"\0")
        return f"-Dcatalina.base={TOMCAT}".encode() in cmdline
    except (OSError, ValueError):
        return False


def tomcat_env():
    return {**os.environ, "CATALINA_BASE": str(TOMCAT), "CATALINA_PID": str(TOMCAT / "tomcat.pid"),
            "CATALINA_OPTS": "-Xms64m -Xmx512m -Dfile.encoding=UTF-8"}


def app_url(number):
    name = os.environ.get("CODESPACE_NAME")
    domain = os.environ.get("GITHUB_CODESPACES_PORT_FORWARDING_DOMAIN", "app.github.dev")
    origin = f"https://{name}-8080.{domain}" if name else "http://127.0.0.1:8080"
    return f"{origin}/lab1-{number}/"


def status():
    print("Console: lab 1 | lab 2 | lab 1 --examples | lab 2 --examples", flush=True)
    print("Domain: lab 11 --domain", flush=True)
    for number in WEB_NUMBERS:
        print(f"1.{number}: {app_url(number)}", flush=True)


def start():
    prepare_databases()
    configure_tomcat()
    if not running():
        with socket.socket() as connection:
            if connection.connect_ex(("127.0.0.1", 8080)) == 0:
                raise RuntimeError("Port 8080 is occupied by another process.")
        for number in WEB_NUMBERS:
            if not (TOMCAT / f"webapps/lab1-{number}.war").exists():
                build_web(number)
        home = Path(os.environ.get("CATALINA_HOME", "/usr/local/tomcat"))
        run([home / "bin/catalina.sh", "start"], env=tomcat_env())
    deadline = time.monotonic() + 120
    for number in WEB_NUMBERS:
        while True:
            try:
                with urllib.request.urlopen(f"http://127.0.0.1:8080/lab1-{number}/", timeout=5) as response:
                    if response.status == 200:
                        break
            except (OSError, urllib.error.URLError):
                pass
            if time.monotonic() > deadline:
                raise RuntimeError(f"Laboratory 1.{number} did not start. See {TOMCAT / 'logs/catalina.out'}")
            time.sleep(1)
    status()


def stop():
    if running():
        home = Path(os.environ.get("CATALINA_HOME", "/usr/local/tomcat"))
        run([home / "bin/catalina.sh", "stop", "15", "-force"], env=tomcat_env())


def main():
    parser = argparse.ArgumentParser(description="Codespaces: lab 1, lab 2, lab 9-15; lab start/stop/status.")
    parser.add_argument("target", nargs="?", default="status")
    parser.add_argument("--examples", nargs="?", type=int, const=0)
    parser.add_argument("--domain", action="store_true")
    args = parser.parse_args()
    if sys.platform != "linux" or os.environ.get("LAB_CONTAINER") != "1":
        parser.error("Use this command inside the prepared Codespace. Windows: use run.ps1.")
    if args.target in ("prepare", "start", "stop", "status"):
        if args.examples is not None or args.domain:
            parser.error("Example and domain options require a laboratory number.")
        if args.target == "prepare":
            prepare_databases()
            configure_tomcat()
            for number in WEB_NUMBERS:
                build_web(number)
        else:
            {"start": start, "stop": stop, "status": status}[args.target]()
        return
    try:
        number = lab_number(args.target)
    except argparse.ArgumentTypeError as error:
        parser.error(str(error))
    if args.examples is not None and number not in (1, 2):
        parser.error("Examples are available only for labs 1 and 2.")
    if args.domain and number != 11:
        parser.error("The domain demonstration is available for lab 11.")
    if args.examples is not None and args.domain:
        parser.error("Choose either examples or the domain demonstration.")
    if number in (1, 2):
        console(number, args.examples)
    elif args.domain:
        show_domain()
    else:
        configure_tomcat()
        build_web(number)
        start()
        print(f"Open laboratory 1.{number}: {app_url(number)}", flush=True)


if __name__ == "__main__":
    try:
        main()
    except (OSError, RuntimeError, ValueError, subprocess.CalledProcessError) as error:
        print(f"ERROR: {error}", file=sys.stderr)
        sys.exit(1)

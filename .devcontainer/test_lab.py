import argparse
import os
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch

import lab


class LauncherTests(unittest.TestCase):
    def test_supported_numbers(self):
        for number in lab.FOLDERS:
            self.assertEqual(lab.lab_number(str(number)), number)
            self.assertEqual(lab.lab_number(f"1.{number}"), number)

    def test_unsupported_numbers(self):
        for value in ("3", "1.8", "0", "16", "../15", "15;echo", "1.1.1"):
            with self.assertRaises(argparse.ArgumentTypeError):
                lab.lab_number(value)

    def test_codespaces_url(self):
        with patch.dict(os.environ, {"CODESPACE_NAME": "test-space",
                                     "GITHUB_CODESPACES_PORT_FORWARDING_DOMAIN": "app.github.dev"}):
            self.assertEqual(lab.app_url(15), "https://test-space-8080.app.github.dev/lab1-15/")

    def test_build_cleanup_is_limited_to_generated_files(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            with patch.object(lab, "STATE", root / ".codespaces"):
                for forbidden in (root, root / "src", lab.STATE):
                    with self.assertRaises(ValueError):
                        lab.clean_build(forbidden)
                generated = lab.STATE / "build/15"
                lab.clean_build(generated)
                (generated / "stale.class").write_bytes(b"old")
                lab.clean_build(generated)
                self.assertFalse((generated / "stale.class").exists())

    def test_existing_databases_are_not_reseeded(self):
        with patch.object(lab.subprocess, "run") as process, patch.object(lab, "psql") as sql, \
                patch.object(lab, "run") as run:
            process.return_value.returncode = 0
            sql.side_effect = ["1", "2"] * 3
            lab.prepare_databases()
            run.assert_not_called()

    def test_incomplete_database_is_not_destroyed(self):
        with patch.object(lab.subprocess, "run") as process, patch.object(lab, "psql") as sql, \
                patch.object(lab, "run") as run:
            process.return_value.returncode = 0
            sql.side_effect = ["1", "1"]
            with self.assertRaisesRegex(RuntimeError, "existing data was not changed"):
                lab.prepare_databases()
            run.assert_not_called()

    def test_tomcat_configuration_is_idempotent_and_proxy_aware(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            home = root / "home"
            (home / "conf").mkdir(parents=True)
            (home / "conf/server.xml").write_text(
                '<Server><Service><Connector protocol="HTTP/1.1"/><Engine/></Service></Server>',
                encoding="utf-8")
            with patch.object(lab, "TOMCAT", root / "runtime"), \
                    patch.dict(os.environ, {"CATALINA_HOME": str(home)}):
                lab.configure_tomcat()
                lab.configure_tomcat()
                tree = lab.ET.parse(lab.TOMCAT / "conf/server.xml")
                connector = tree.find("./Service/Connector")
                self.assertEqual(connector.get("address"), "127.0.0.1")
                self.assertEqual(connector.get("port"), "8080")
                valves = tree.findall("./Service/Engine/Valve")
                self.assertEqual(len(valves), 1)
                self.assertEqual(valves[0].get("protocolHeader"), "x-forwarded-proto")


if __name__ == "__main__":
    unittest.main()

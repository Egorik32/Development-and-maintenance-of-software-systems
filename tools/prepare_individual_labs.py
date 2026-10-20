"""Materialize cumulative lab snapshots from the existing projects and reviewed templates."""
import json
import re
import shutil
from pathlib import Path
from xml.etree import ElementTree as ET
from pypdf import PdfReader
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
TEMPLATES = Path(__file__).parent / "templates"
LABS = json.loads((Path(__file__).parent / "lab_conditions.json").read_text(encoding="utf-8"))
FX = ROOT / "CityParksJavaFX"
EE = ROOT / "CityParksJavaEE"
PKG = "src/main/java/com/example/cityparks/"
RES = "src/main/resources/com/example/cityparks/"


def write(folder, name, text):
    path = folder / name
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding="utf-8")


def copy(folder, name, source):
    dest = folder / name
    dest.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(source, dest)


def without_method(text, name):
    match = re.search(r"(?m)^    (?:public|private|protected|static)[^\n]*\b" + name + r"\([^)]*\)\s*(?:throws [^{]+)?\{", text)
    if not match:
        raise ValueError("Method not found: " + name)
    start = match.start()
    for annotation in ("    @FXML\n", "    @Override\n"):
        if text[:start].endswith(annotation):
            start -= len(annotation)
    pos = text.index("{", match.start())
    depth = 1
    end = pos + 1
    while depth:
        depth += (text[end] == "{") - (text[end] == "}")
        end += 1
    return text[:start] + text[end:].lstrip("\n")


FX_BUILD = r'''$ErrorActionPreference = "Stop"
. (Join-Path (Split-Path $PSScriptRoot -Parent) "runtime\java_env.ps1")
$src = Join-Path $PSScriptRoot "src\main\java"
$out = Join-Path $PSScriptRoot "out"
New-Item -ItemType Directory -Force -Path $out | Out-Null
$sources = @(Get-ChildItem -LiteralPath $src -Recurse -Filter *.java | ForEach-Object FullName)
& "$env:JAVA_HOME\bin\javac.exe" -encoding UTF-8 -d $out $sources
if ($LASTEXITCODE -ne 0) { throw "Compilation failed." }
Copy-Item -Path (Join-Path $PSScriptRoot "src\main\resources\*") -Destination $out -Recurse -Force
Write-Host "Compilation completed."
'''
FX_RUN = r'''$ErrorActionPreference = "Stop"
& (Join-Path $PSScriptRoot "build.ps1")
. (Join-Path (Split-Path $PSScriptRoot -Parent) "runtime\java_env.ps1")
& "$env:JAVA_HOME\bin\java.exe" "-Dfile.encoding=UTF-8" "-Dcityparks.lab=LABNUMBER" -cp (Join-Path $PSScriptRoot "out") com.example.cityparks.MainApp
if ($LASTEXITCODE -ne 0) { throw "Application exited with an error." }
'''
EE_BUILD = r'''$ErrorActionPreference = "Stop"
. (Join-Path (Split-Path $PSScriptRoot -Parent) "runtime\java17_env.ps1")
$out = Join-Path $PSScriptRoot "out"
$build = Join-Path $PSScriptRoot "build"
$dist = Join-Path $PSScriptRoot "dist"
New-Item -ItemType Directory -Force -Path $out,$build,$dist | Out-Null
$web = Join-Path $PSScriptRoot "src\main\webapp"
Copy-Item -Path "$web\*" -Destination $build -Recurse -Force
$lib = Join-Path $web "WEB-INF\lib"
$sources = @(Get-ChildItem -LiteralPath (Join-Path $PSScriptRoot "src\main\java") -Recurse -Filter *.java | ForEach-Object FullName)
if ($sources.Count -gt 0) {
    & "$env:JAVA_HOME\bin\javac.exe" -encoding UTF-8 -cp "$lib\*" -d $out $sources
    if ($LASTEXITCODE -ne 0) { throw "Compilation failed." }
    $classes = Join-Path $build "WEB-INF\classes"
    New-Item -ItemType Directory -Force -Path $classes | Out-Null
    Copy-Item -Path "$out\*" -Destination $classes -Recurse -Force
    $config = Join-Path $PSScriptRoot "src\main\java\config"
    if (Test-Path -LiteralPath $config) { Copy-Item -LiteralPath $config -Destination $classes -Recurse -Force }
}
$servletApi = Join-Path $build "WEB-INF\lib\jakarta.servlet-api-5.0.0.jar"
if (Test-Path -LiteralPath $servletApi) { Remove-Item -LiteralPath $servletApi }
& "$env:JAVA_HOME\bin\jar.exe" --create --file (Join-Path $dist "CONTEXT.war") -C $build .
if ($LASTEXITCODE -ne 0) { throw "WAR packaging failed." }
Write-Host "WAR created: dist\CONTEXT.war"
'''
EE_RUN = r'''$ErrorActionPreference = "Stop"
param([int]$Port = 8080)
& (Join-Path $PSScriptRoot "build.ps1")
. (Join-Path (Split-Path $PSScriptRoot -Parent) "runtime\java17_env.ps1")
$tomcat = Join-Path (Split-Path $PSScriptRoot -Parent) "runtime\apache-tomcat-10.0.27"
$base = Join-Path $PSScriptRoot ".tomcat"
New-Item -ItemType Directory -Force -Path "$base\conf","$base\logs","$base\temp","$base\work","$base\webapps" | Out-Null
Copy-Item -Path "$tomcat\conf\*" -Destination "$base\conf" -Force
$server = [xml](Get-Content -LiteralPath "$base\conf\server.xml")
$server.Server.port = "-1"
$http = $server.Server.Service.Connector | Where-Object protocol -eq "HTTP/1.1"
$http.port = [string]$Port
$http.SetAttribute("address", "127.0.0.1")
$server.Save("$base\conf\server.xml")
Copy-Item -LiteralPath (Join-Path $PSScriptRoot "dist\CONTEXT.war") -Destination "$base\webapps\CONTEXT.war" -Force
$env:CATALINA_HOME = $tomcat
$env:CATALINA_BASE = $base
$env:CATALINA_OPTS = "-Dfile.encoding=UTF-8"
Write-Host "Open http://localhost:$Port/CONTEXT/"
Write-Host "Press Ctrl+C to stop Tomcat before opening another lab."
& "$tomcat\bin\catalina.bat" run
'''
# PowerShell parameters must precede executable statements.
EE_RUN = EE_RUN.replace('$ErrorActionPreference = "Stop"\nparam([int]$Port = 8080)', 'param([int]$Port = 8080)\n$ErrorActionPreference = "Stop"')


def fx_snapshot(lab, folder):
    n = lab["number"]
    for path in (FX / "src").rglob("*"):
        if path.is_file():
            rel = path.relative_to(FX).as_posix()
            if "/util/" in rel or "EditDialog" in rel or rel.endswith("RootLayoutController.java"):
                continue
            if n == 3 and "/model/" in rel:
                continue
            copy(folder, rel, path)
    main = (FX / PKG / "MainApp.java").read_text(encoding="utf-8")
    main = main.replace('import com.example.cityparks.controller.RootLayoutController;\n', '')
    main = main.replace('            RootLayoutController controller = loader.getController();\n            controller.setMainApp(this);\n', '')
    main = main.replace('            controller.setParks(parkData);\n', '')
    main = main.replace('            ex.printStackTrace();', '            throw new IllegalStateException(ex);')
    main = main.replace('            throw new IllegalStateException(ex);\n            return false;', '            throw new IllegalStateException(ex);')
    if n < 7:
        main = main.replace('import com.example.cityparks.util.XmlDataStore;\n', '')
        for method in ("getCityFilePath", "setCityFilePath", "loadCityDataFromFile", "saveCityDataToFile"):
            main = without_method(main, method)
    else:
        main = main.replace('        showCityOverview();', '''        showCityOverview();
        File lastFile = getCityFilePath();
        if (lastFile != null && lastFile.isFile()) {
            try { loadCityDataFromFile(lastFile); }
            catch (Exception ex) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                alert.initOwner(primaryStage);
                alert.setHeaderText("Не удалось открыть последний XML-файл");
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
            }
        }''')
        main = main.replace('Preferences.userNodeForPackage(MainApp.class)', 'Preferences.userNodeForPackage(MainApp.class).node(System.getProperty("cityparks.lab", "1.8"))')
        main = main.replace('file.getPath()', 'file.getAbsolutePath()')
        copy(folder, PKG + "model/CityListWrapper.java", TEMPLATES / "CityListWrapper.java")
        copy(folder, PKG + "util/XmlDataStore.java", TEMPLATES / "XmlDataStore.java")
        copy(folder, PKG + "util/LocalDateAdapter.java", TEMPLATES / "LocalDateAdapter.java")
        park = (folder / PKG / "model/Park.java").read_text(encoding="utf-8")
        park = park.replace('    public LocalDate getBuildDate()', '    @javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter(com.example.cityparks.util.LocalDateAdapter.class)\n    public LocalDate getBuildDate()')
        write(folder, PKG + "model/Park.java", park)
        copy(folder, PKG + "controller/RootLayoutController.java", FX / PKG / "controller/RootLayoutController.java")
        root = (folder / PKG / "controller/RootLayoutController.java").read_text(encoding="utf-8")
        root = root.replace('        mainApp.getCityData().clear();', '        mainApp.getCityData().clear();\n        mainApp.getParkData().clear();')
        root = root.replace('        System.exit(0);', '        javafx.application.Platform.exit();')
        root = without_method(root, "handleAbout")
        write(folder, PKG + "controller/RootLayoutController.java", root)
        main = main.replace('            primaryStage.show();', '''            com.example.cityparks.controller.RootLayoutController rootController = loader.getController();
            rootController.setMainApp(this);
            primaryStage.show();''')
    if n < 5:
        main = without_method(main, "showCityEditDialog")
        main = main.replace('import com.example.cityparks.controller.CityEditDialogController;\n', '')
    else:
        copy(folder, PKG + "controller/CityEditDialogController.java", TEMPLATES / "CityEditDialogController.java")
        dialog = (FX / RES / "view/CityEditDialog.fxml").read_text(encoding="utf-8")
        dialog = dialog.replace('<?import javafx.scene.control.ComboBox?>\n', '')
        dialog = dialog.replace('<ComboBox fx:id="parkComboBox" maxWidth="Infinity"', '<TextField fx:id="parkNameField"')
        write(folder, RES + "view/CityEditDialog.fxml", dialog)
    if n == 3:
        main = without_method(main, "MainApp")
        main = without_method(main, "getCityData")
        main = without_method(main, "getParkData")
        main = re.sub(r'    private final ObservableList<[^\n]+\n', '', main)
        main = re.sub(r'import com\.example\.cityparks\.model\.[^\n]+\n', '', main)
        main = main.replace('            controller.setMainApp(this);\n', '')
        write(folder, PKG + "controller/CityOverviewController.java", 'package com.example.cityparks.controller;\n\npublic class CityOverviewController { }\n')
    elif n == 4:
        controller = (FX / PKG / "controller/CityOverviewController.java").read_text(encoding="utf-8")
        for name in ("handleDeleteCity", "handleNewCity", "handleEditCity", "showWarning"):
            controller = without_method(controller, name)
        controller = controller.replace('        cityTable.getSelectionModel().selectedItemProperty()\n                .addListener((observable, oldValue, newValue) -> showCityDetails(newValue));\n', '')
        write(folder, PKG + "controller/CityOverviewController.java", controller)
    if n < 6:
        main = main.replace('            scene.getStylesheets().add(MainApp.class.getResource("css/style.css").toExternalForm());\n', '')
        (folder / RES / "css/style.css").unlink()
    else:
        main = main.replace('        this.primaryStage.setTitle("Города и парки");', '''        this.primaryStage.setTitle("Города и парки");
        this.primaryStage.getIcons().add(new javafx.scene.image.Image(MainApp.class.getResourceAsStream("images/park.png")));''')
        main = main.replace('            dialogStage.setScene(new Scene(page));', '''            Scene dialogScene = new Scene(page);
            dialogScene.getStylesheets().add(MainApp.class.getResource("css/style.css").toExternalForm());
            dialogStage.setScene(dialogScene);
            dialogStage.getIcons().addAll(primaryStage.getIcons());''')
        im = Image.new("RGBA", (64, 64), (255, 255, 255, 255))
        d = ImageDraw.Draw(im)
        d.rounded_rectangle((3, 3, 61, 61), radius=8, fill="#287c48")
        d.rectangle((30, 32, 35, 54), fill="#f5e6b4")
        d.ellipse((14, 9, 50, 44), fill="#b9e38e")
        icon = folder / RES / "images/park.png"
        icon.parent.mkdir(parents=True, exist_ok=True)
        im.save(icon)
    write(folder, PKG + "MainApp.java", main)
    for path in (folder / "src/main/resources").rglob("*.fxml"):
        text = path.read_text(encoding="utf-8").replace('javafx/23.0.1', 'javafx/8.0.504')
        if path.name == "RootLayout.fxml":
            text = text.replace('<BorderPane ', '<BorderPane prefWidth="960.0" prefHeight="560.0" ')
            text = re.sub(r'\s*<Menu text="Справка">.*?</Menu>', '', text, flags=re.S)
            if n < 7:
                text = re.sub(r'\s*fx:controller="[^"]+"', '', text)
                text = re.sub(r' onAction="[^"]+"', ' disable="true"', text)
        if n < 5:
            text = re.sub(r' onAction="[^"]+"', ' disable="true"', text)
        text = text.replace('<Label fx:id=', '<Label wrapText="true" fx:id=')
        write(folder, path.relative_to(folder), text)
    write(folder, "build.ps1", FX_BUILD)
    write(folder, "run.ps1", FX_RUN.replace("LABNUMBER", f"1.{n}"))
    write(folder, ".classpath", '<classpath><classpathentry kind="src" path="src/main/java"/><classpathentry kind="src" path="src/main/resources"/><classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.8"/><classpathentry kind="output" path="out"/></classpath>\n')


def ee_snapshot(lab, folder):
    n = lab["number"]
    context = f"lab1-{n}"
    (folder / "src/main/java").mkdir(parents=True, exist_ok=True)
    for path in (EE / "src").rglob("*"):
        if not path.is_file(): continue
        rel = path.relative_to(EE).as_posix()
        if "/java/" in rel:
            if n < 10: continue
            if "/domain/" in rel and n < 11: continue
            if ("/dao/" in rel or "/config/" in rel) and n < 13: continue
            if "/controller/" in rel and (path.stem.startswith("Edit") or path.stem.startswith("Delete")) and n < 15: continue
        if rel.endswith(("city.jsp", "park.jsp")) and not path.stem.startswith("edit") and n < 12: continue
        if path.stem.startswith("edit") and n < 15: continue
        if "postgresql" in rel and n < 13: continue
        copy(folder, rel, path)
    if n >= 10:
        for entity in ("City", "Park"):
            rel = f"src/main/java/controller/{entity}Servlet.java"
            if n < 12:
                code = '''package controller;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
@WebServlet({"/ENTITYLOW", "/HelloENTITYServlet"})
public class ENTITYServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html; charset=UTF-8");
        response.getWriter().println("<h2>Привет ENTITYServlet</h2><a href='" + request.getContextPath() + "/'>Главная</a>");
    }
    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
'''.replace("ENTITYLOW", entity.lower()).replace("ENTITY", entity)
            else:
                code = (EE / rel).read_text(encoding="utf-8")
                if n < 14:
                    code = without_method(code, "doPost")
                    code = without_method(code, "read" + entity)
                if n == 12:
                    code = re.sub(r'import dao\.[^\n]+\n', '', code)
                    code = re.sub(r'    private final [^\n]+DAO[^\n]+\n', '', code)
                    start = code.index('        try {')
                    code = code[:start] + '''        Park gorky = new Park(1L, "Парк имени Горького", "городской", new java.math.BigDecimal("19.50"), true, java.time.LocalDate.of(1928, 8, 12));
        Park revolution = new Park(2L, "Парк Революции", "культурный", new java.math.BigDecimal("17.00"), false, java.time.LocalDate.of(1926, 5, 1));
        Park olympic = new Park(3L, "Олимпийский парк", "спортивный", new java.math.BigDecimal("200.00"), true, java.time.LocalDate.of(2014, 2, 7));
        request.setAttribute("parks", java.util.Arrays.asList(gorky, revolution, olympic));
''' + ('''        request.setAttribute("cities", java.util.Arrays.asList(
            new domain.City(1L, "Ростов-на-Дону", "областной центр", 1142162, gorky),
            new domain.City(2L, "Шахты", "город областного значения", 226452, revolution),
            new domain.City(3L, "Сочи", "курортный город", 466078, olympic)));
''' if entity == "City" else '') + f'        request.getRequestDispatcher("/{entity.lower()}.jsp").forward(request, response);\n    }}\n}}\n'
                    if 'import domain.Park;' not in code: code = code.replace('package controller;', 'package controller;\nimport domain.Park;')
            write(folder, rel, code)
    if n >= 11:
        for entity in ("City", "Park"):
            rel = f"src/main/java/domain/{entity}.java"
            text = (folder / rel).read_text(encoding="utf-8")
            if entity == "City":
                text = text.replace('    private Park park;', '    private Long idPark;\n    private Park park;')
                text = text.replace('        this.park = park;', '        this.park = park;\n        this.idPark = park == null ? null : park.getId();')
                extra = '''    public Long getIdPark() { return idPark; }
    public void setIdPark(Long idPark) { this.idPark = idPark; }
    @Override public String toString() {
        return "City {id=" + id + ", name=" + name + ", status=" + status + ", population=" + population + ", idPark=" + idPark + ", park=" + park + "}";
    }
'''
            else:
                extra = '''    @Override public String toString() {
        return "Park {id=" + id + ", name=" + name + ", type=" + type + ", area=" + area + ", hasWater=" + hasWater + ", buildDate=" + buildDate + "}";
    }
'''
            write(folder, rel, text.rstrip()[:-1] + extra + '}\n')
    if n >= 13:
        for entity in ("City", "Park"):
            rel = f"src/main/java/dao/{entity}DbDAO.java"
            text = (folder / rel).read_text(encoding="utf-8")
            if n < 15:
                for method in ("update", "delete"): text = without_method(text, method)
            if n < 14:
                text = without_method(text, "insert")
                text = without_method(text, "fill" + entity + "Statement")
            write(folder, rel, text)
        config = (EE / "src/main/java/config/config.properties").read_text(encoding="utf-8")
        config = config.replace('/cityparks', '/' + f'cityparks_lab{n}')
        write(folder, "src/main/java/config/config.properties", config)
        copy(folder, "sql/schema.sql", EE / "sql/schema.sql")
        setup = (EE / "setup_db.ps1").read_text(encoding="utf-8")
        setup = setup.replace('$dbName = "cityparks"', f'$dbName = "cityparks_lab{n}"')
        setup = setup.replace('-d $dbName -f $schema', '-d $dbName -v ON_ERROR_STOP=1 -f $schema')
        write(folder, "setup_db.ps1", setup)
    for path in (folder / "src/main/webapp").rglob("*.jsp"):
        text = path.read_text(encoding="utf-8").replace('<div class="panel">', '<div>')
        if n < 15:
            text = re.sub(r'<th>Действия</th>', '', text)
            text = re.sub(r'\s*<td class="table-actions">.*?</td>', '', text, flags=re.S)
        else:
            text = re.sub(r'(<a class="btn btn-sm btn-outline-danger")', r'\1 onclick="return confirm(\'Удалить запись?\')"', text)
        if n < 14:
            text = text.replace('<button class="btn btn-primary" type="submit">', '<button class="btn btn-primary" type="submit" disabled>')
        write(folder, path.relative_to(folder), text)
    index = (EE / "src/main/webapp/index.jsp").read_text(encoding="utf-8")
    start = index.index('        <h1')
    end = index.index('    </main>')
    index = index[:start] + '''        <h1 class="page-title">Главное меню</h1>
        <ul class="list-group list-group-flush">
            <li class="list-group-item"><a href="<c:url value='/city' />">Города</a></li>
            <li class="list-group-item"><a href="<c:url value='/park' />">Парки</a></li>
        </ul>
''' + index[end:]
    if n == 9: index = index.replace("value='/city'", "value='#'").replace("value='/park'", "value='#'")
    write(folder, "src/main/webapp/index.jsp", index)
    header = (folder / "src/main/webapp/views/header.jsp").read_text(encoding="utf-8")
    header = header.replace('>Города и парки</a>', '><img src="<c:url value=\'/images/park.png\' />" width="32" height="32" alt="Города и парки"> Города и парки</a>')
    if n == 9: header = header.replace("value='/city'", "value='#'").replace("value='/park'", "value='#'")
    write(folder, "src/main/webapp/views/header.jsp", header)
    copy(folder, "src/main/webapp/images/park.png", ROOT / LABS[3]["folder"] / RES / "images/park.png")
    write(folder, "src/main/webapp/WEB-INF/web.xml", '<?xml version="1.0" encoding="UTF-8"?><web-app xmlns="https://jakarta.ee/xml/ns/jakartaee" version="5.0"><display-name>Города и парки</display-name><welcome-file-list><welcome-file>index.jsp</welcome-file></welcome-file-list></web-app>\n')
    write(folder, "build.ps1", EE_BUILD.replace("CONTEXT", context))
    write(folder, "run.ps1", EE_RUN.replace("CONTEXT", context))
    write(folder, "build_war.ps1", '& (Join-Path $PSScriptRoot "build.ps1")\n')
    write(folder, ".classpath", '<classpath><classpathentry kind="src" path="src/main/java"/><classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-17"/><classpathentry kind="con" path="org.eclipse.jst.server.core.container/org.eclipse.jst.server.tomcat.runtimeTarget/Apache Tomcat v10.0"/><classpathentry kind="output" path="out"/></classpath>\n')
    write(folder, ".settings/org.eclipse.wst.common.project.facet.core.xml", '<faceted-project><fixed facet="java"/><fixed facet="jst.web"/><installed facet="java" version="17"/><installed facet="jst.web" version="5.0"/></faceted-project>\n')
    write(folder, ".settings/org.eclipse.wst.common.component", f'<project-modules id="moduleCoreId" project-version="1.5.0"><wb-module deploy-name="{context}"><wb-resource deploy-path="/" source-path="/src/main/webapp"/><wb-resource deploy-path="/WEB-INF/classes" source-path="/src/main/java"/><property name="context-root" value="{context}"/><property name="java-output-path" value="/{lab["folder"]}/out"/></wb-module></project-modules>\n')


def main():
    for lab in LABS:
        folder = ROOT / lab["folder"]
        if folder.exists(): raise FileExistsError("Snapshot already exists: " + str(folder))
        folder.mkdir()
        n = lab["number"]
        url = f'https://do.rsue.ru/mod/assign/view.php?id={129763+n}'
        if n < 9:
            condition = f'Лабораторная работа 1.{n}. {lab["title"]}\n\n{lab["tutorial"]}\n\n{lab["condition"]}\n'
            fx_snapshot(lab, folder)
        else:
            pdf = ROOT / "materials" / lab["pdf"]
            text = '\n\n'.join(p.extract_text() or '' for p in PdfReader(pdf).pages)
            write(folder, "МЕТОДИЧКА_ПОЛНЫЙ_ТЕКСТ.txt", text)
            copy(folder, "УСЛОВИЕ_ОРИГИНАЛ.pdf", pdf)
            marker = 'Задание на лабораторную работу'
            condition = text[text.rfind(marker):].strip() + '\n'
            ee_snapshot(lab, folder)
        write(folder, "ЗАДАНИЕ.txt", f'Источник: {url}\n\n' + condition)
        write(folder, "ВАРИАНТ.txt", 'Вариант 12\nГорода: Название, Статус, Население.\nПарки: Название, Тип, Площадь, Наличие водоема, Дата постройки.\nТехнические идентификаторы и связь с парком нужны для базы данных, как связь сущностей в примере методички.\n')
        natures = '<nature>org.eclipse.jdt.core.javanature</nature>'
        if n >= 9: natures += '<nature>org.eclipse.wst.common.project.facet.core.nature</nature><nature>org.eclipse.wst.common.modulecore.ModuleCoreNature</nature>'
        write(folder, ".project", f'<projectDescription><name>{lab["folder"]}</name><comment>ЛР 1.{n}, вариант 12</comment><projects/><buildSpec><buildCommand><name>org.eclipse.jdt.core.javabuilder</name><arguments/></buildCommand></buildSpec><natures>{natures}</natures></projectDescription>\n')
        write(folder, ".gitignore", 'out/\nbuild/\ndist/\n.tomcat/\nverification/\n*.log\n')
        command = '.\\run.ps1'
        if n >= 13: command = '.\\setup_db.ps1\n.\\run.ps1'
        extra = ''
        if n < 5: extra = 'На этом этапе кнопки изменения данных еще не реализуются: они добавляются в ЛР 1.5.\n'
        if n == 9: extra = 'Главная страница и меню. Ссылки на данные подключаются в ЛР 1.10.\n'
        if n in (12, 13): extra = 'Кнопка добавления пока отключена: обработка POST появляется в ЛР 1.14.\n'
        if n >= 13: extra += f'База данных: cityparks_lab{n}. Подготовка базы сбрасывает только тестовые таблицы этой лабораторной. Не запускайте setup_db.ps1 после добавления данных, которые хотите сохранить.\n'
        write(folder, "README.md", f'# Лабораторная работа 1.{n}\n\n{lab["title"]}. Вариант 12.\n\nУсловие: `ЗАДАНИЕ.txt`. ' + ('Полная методичка и оригинальный PDF находятся в этой папке.\n\n' if n >= 9 else f'Учебник: {lab["tutorial"]}\n\n') + f'Откройте эту папку в VS Code, затем терминал PowerShell:\n\n```powershell\n{command}\n```\n\n' + (f'Адрес: http://localhost:8080/lab1-{n}/\nЗакройте предыдущий сервер через Ctrl+C перед запуском следующей лабораторной. Другой порт: `.\\run.ps1 -Port 8082`.\n\n' if n >= 9 else 'Java 8 с JavaFX берется из соседней папки `runtime`, системный путь к Java не используется.\n\n') + extra)
        print(lab["folder"], flush=True)


if __name__ == '__main__': main()

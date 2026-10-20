const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const { chromium } = require("playwright");

async function main() {
    const [base, directory] = process.argv.slice(2);
    assert(base && directory, "Provide the lab URL and verification directory");
    const options = fs.existsSync(chromium.executablePath())
        ? { executablePath: chromium.executablePath() }
        : { channel: "msedge" };
    const browser = await chromium.launch({ ...options, headless: true });
    const errors = [];
    const checks = [];
    try {
        const page = await browser.newPage({ viewport: { width: 1440, height: 900 } });
        page.on("pageerror", error => errors.push(error.message));
        page.on("requestfailed", request => errors.push(request.url()));
        let response = await page.goto(base + "/city", { waitUntil: "networkidle" });
        assert.match(response.headers()["content-type"], /charset=UTF-8/i);
        let body = await page.locator("body").innerText();
        for (const value of ["Города", "Ростов-на-Дону", "Шахты", "Сочи", "областной центр", "город областного значения", "курортный город", "Парк имени Горького"]) {
            assert(body.includes(value), "City page missing correct Russian text: " + value);
        }
        await page.screenshot({ path: path.join(directory, "utf8-cities.png"), fullPage: true });
        const cityCells = await page.locator("tbody tr").first().locator("td").allTextContents();
        const cityEditLink = await page.locator("tbody tr").first().getByRole("link", { name: "Редактировать", exact: true }).getAttribute("href");
        checks.push("Browser city names, statuses and headers display correct UTF-8");
        const dialog = page.waitForEvent("dialog");
        const click = page.locator("tbody tr").first().getByRole("link", { name: "Удалить", exact: true }).click();
        const confirmation = await dialog;
        assert.equal(confirmation.message(), "Удалить запись?");
        await confirmation.dismiss();
        await click;
        checks.push("Deletion confirmation is readable; cancellation preserves the record");
        response = await page.goto(base + "/park", { waitUntil: "networkidle" });
        assert.equal(response.status(), 200);
        body = await page.locator("body").innerText();
        for (const value of ["Парки", "Парк имени Горького", "Парк Революции", "Олимпийский парк", "городской", "культурный", "спортивный"]) {
            assert(body.includes(value), "Park page missing correct Russian text: " + value);
        }
        await page.screenshot({ path: path.join(directory, "utf8-parks.png"), fullPage: true });
        const parkCells = await page.locator("tbody tr").first().locator("td").allTextContents();
        const parkEditLink = await page.locator("tbody tr").first().getByRole("link", { name: "Редактировать", exact: true }).getAttribute("href");
        checks.push("Browser park names and types display correct UTF-8");
        await page.goto(new URL(cityEditLink, base).href, { waitUntil: "networkidle" });
        assert.equal(await page.locator('input[name="name"]').inputValue(), cityCells[1].trim());
        assert.equal(await page.locator('input[name="status"]').inputValue(), cityCells[2].trim());
        await page.goto(new URL(parkEditLink, base).href, { waitUntil: "networkidle" });
        assert.equal(await page.locator('input[name="name"]').inputValue(), parkCells[1].trim());
        assert.equal(await page.locator('input[name="type"]').inputValue(), parkCells[2].trim());
        checks.push("City and park edit forms contain readable Russian values");
        assert.deepEqual(errors, []);
        fs.writeFileSync(path.join(directory, "utf8-browser-result.json"), JSON.stringify({ passed: true, checks, errors }, null, 2));
        checks.forEach(check => console.log("PASS: " + check));
    } finally {
        await browser.close();
    }
}

main().catch(error => {
    console.error(error);
    process.exitCode = 1;
});

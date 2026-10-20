const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const { chromium } = require("playwright");

async function main() {
    const [url, outputDir] = process.argv.slice(2);
    assert(url && outputDir, "Provide a lab URL and verification directory.");
    const options = fs.existsSync(chromium.executablePath())
        ? { executablePath: chromium.executablePath() }
        : { channel: "msedge" };
    const browser = await chromium.launch({ ...options, headless: true });
    const checks = [];
    const errors = [];
    try {
        const page = await browser.newPage({ viewport: { width: 1366, height: 768 } });
        page.on("pageerror", error => errors.push(error.message));
        page.on("requestfailed", request => errors.push(request.url()));
        const response = await page.goto(url, { waitUntil: "networkidle" });
        assert.equal(response.status(), 200);
        const versions = await page.evaluate(() => ({
            jquery: window.jQuery.fn.jquery,
            bootstrap: window.bootstrap.Popover.VERSION,
            plugin: typeof window.jQuery.fn.popover,
            iconLoaded: document.querySelector(".navbar-brand img").naturalWidth > 0
        }));
        assert.equal(versions.jquery, "3.6.4");
        assert.equal(versions.bootstrap, "5.3.3");
        assert.equal(versions.plugin, "function");
        assert.equal(versions.iconLoaded, true);
        checks.push("jQuery 3.6.4 and Bootstrap 5 load locally; jQuery popover plugin is available");
        const button = page.locator("#bootstrap-check");
        const popover = page.locator(".popover.show .popover-body");
        await button.hover();
        await popover.waitFor({ state: "visible" });
        await page.waitForFunction(() => getComputedStyle(document.querySelector('.popover.show')).opacity === '1');
        assert.equal(await popover.textContent(), "Ура, Bootstrap 5 работает");
        const footerBox = await page.locator("footer").boundingBox();
        const popoverBox = await page.locator(".popover.show").boundingBox();
        const imageHeight = Math.ceil(Math.max(footerBox.y + footerBox.height, popoverBox.y + popoverBox.height) + 16);
        await page.screenshot({ path: path.join(outputDir, "bootstrap-desktop.png"), clip: { x: 0, y: 0, width: 1366, height: imageHeight } });
        checks.push("Hover displays the required Bootstrap message");
        await page.mouse.move(1, 1);
        await popover.waitFor({ state: "hidden" });
        checks.push("Popover hides after mouse leave");
        await page.setViewportSize({ width: 390, height: 844 });
        await page.locator(".navbar-toggler").click();
        await page.locator("#navbarContent.show").waitFor({ state: "visible" });
        await button.focus();
        await popover.waitFor({ state: "visible" });
        await page.waitForFunction(() => getComputedStyle(document.querySelector('.popover.show')).opacity === '1');
        const box = await page.locator(".popover.show").boundingBox();
        assert(box.x >= 0 && box.x + box.width <= 391, "Popover must fit the mobile viewport.");
        await page.screenshot({ path: path.join(outputDir, "bootstrap-mobile.png"), fullPage: true });
        checks.push("Mobile navigation and keyboard-focus popover work without overflow");
        assert.deepEqual(errors, [], "Browser errors or failed resource requests");
        fs.writeFileSync(path.join(outputDir, "bootstrap_result.json"), JSON.stringify({ passed: true, versions, checks, errors }, null, 2));
        checks.forEach(check => console.log("PASS: " + check));
    } finally {
        await browser.close();
    }
}

main().catch(error => {
    console.error(error);
    process.exitCode = 1;
});

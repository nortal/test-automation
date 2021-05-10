pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
    pm.environment.set("_line-id", pm.response.json().cart.lines.pop().id);

    pm.environment.set("_current-line-no", pm.environment.get("_next-line-no"));
    pm.environment.set("_next-line-no", pm.environment.get("_next-line-no") + 1);
});
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);

    Object.getOwnPropertyNames(pm.environment.toObject())
        .filter(key => key.startsWith('_'))
        .forEach(key => pm.environment.unset(key));

    pm.environment.set("_jwt-token", pm.response.json().access_token);
    pm.environment.set("_current-line-no", 1);
    pm.environment.set("_next-line-no", 1);
    // pm.environment.set("appId", 'TMO');
    var interactionIdPrefix = pm.variables.get("interactionIdPrefix") || '';
    pm.environment.set("_interactionId", interactionIdPrefix + (new Date().getTime()));
});
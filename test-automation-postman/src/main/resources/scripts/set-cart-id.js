pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
    pm.environment.set("_cart-id", pm.response.json().cart.cartId);
});
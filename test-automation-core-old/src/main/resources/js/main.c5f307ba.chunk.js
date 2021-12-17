(this["webpackJsonpjs-view"] = this["webpackJsonpjs-view"] || []).push([[0], {
    4: function (e, t, n) {
        e.exports = n(5)
    }, 5: function (e, t, n) {
        "use strict";
        n.r(t);
        var a = n(0), o = n.n(a), r = n(2), c = n.n(r), s = n(3), i = n.n(s);
        document.querySelectorAll(".embedding.indention").forEach((function (e) {
            e.onclick = function () {
                var t = e.getElementsByClassName("json-view");
                Array.from(t).forEach((function (e) {
                    var t = JSON.parse(atob(e.getAttribute("data-input-json")));
                    c.a.render(o.a.createElement(i.a, {
                        src: t,
                        theme: "rjv-default",
                        collapsed: 8,
                        shouldCollapse: (field) => { return field.name === 'headers' },
                        iconStyle: "circle",
                        displayDataTypes: !1,
                        collapseStringsAfterLength: 50
                    }), e)
                }))
            }
        }))
    }
}, [[4, 1, 2]]]);
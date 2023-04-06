/*
 * Copyright (c) 2022 Nortal AS
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
'use strict';

var LogsTabModel = Backbone.Collection.extend({
    url: 'data/logs.json'
})

const template = function (data) {
    //existing impl supports a single log file, but can be extended to support multiple files
    const logFileName=data.items[0].attributes.filename;

    return `
    <div class="log-window">
        <h3 class="pane__title">Test Framework logs:</h3>
        <iframe src="data/${logFileName}"  class="log-window"></iframe>
    </div>
    `;
}

var logsView = Backbone.Marionette.View.extend({
    template: template,
    render: function () {
        this.$el.html(this.template(this.options));
        return this;
    }
})

class LogsLayout extends allure.components.AppLayout {
    initialize() {
        this.model = new LogsTabModel();
    }

    loadData() {
        return this.model.fetch();
    }

    getContentView() {
        return new logsView({items: this.model.models});
    }
}

allure.api.addTab('logs', {
    title: 'Logs', icon: 'fa fa-book',
    route: 'logs',
    onEnter: (function () {
        return new LogsLayout()
    })
});
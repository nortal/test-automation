'use strict';

var LogsTabModel = Backbone.Collection.extend({
    url: 'data/logs.json'
})

const template = function (data) {
    console.log(data)
    let content=data.items[0].attributes.content

    return `
    <h3 class="pane__title">Test Framework logs:</h3>
    <div class="attachment">
        <div class="attachment__text-container">
            <pre class="attachment__text">${content}</pre>
        </div>
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
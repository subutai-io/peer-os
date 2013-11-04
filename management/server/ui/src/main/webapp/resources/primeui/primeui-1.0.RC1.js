/*
 * Copyright 2009-2012 Prime Teknoloji.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * PUI Object
 */
PUI = {

    zindex: 1000,

    /**
     *  Aligns container scrollbar to keep item in container viewport, algorithm copied from jquery-ui menu widget
     */
    scrollInView: function (container, item) {
        var borderTop = parseFloat(container.css('borderTopWidth')) || 0,
            paddingTop = parseFloat(container.css('paddingTop')) || 0,
            offset = item.offset().top - container.offset().top - borderTop - paddingTop,
            scroll = container.scrollTop(),
            elementHeight = container.height(),
            itemHeight = item.outerHeight(true);

        if (offset < 0) {
            container.scrollTop(scroll + offset);
        }
        else if ((offset + itemHeight) > elementHeight) {
            container.scrollTop(scroll + offset - elementHeight + itemHeight);
        }
    },

    isIE: function (version) {
        return ($.browser.msie && parseInt($.browser.version, 10) === version);
    },

    escapeRegExp: function (text) {
        return text.replace(/([.?*+^$[\]\\(){}|-])/g, "\\$1");
    },

    escapeHTML: function (value) {
        return value.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    },

    clearSelection: function () {
        if (window.getSelection) {
            if (window.getSelection().empty) {
                window.getSelection().empty();
            } else if (window.getSelection().removeAllRanges) {
                window.getSelection().removeAllRanges();
            }
        } else if (document.selection && document.selection.empty) {
            document.selection.empty();
        }
    },

    inArray: function (arr, item) {
        for (var i = 0; i < arr.length; i++) {
            if (arr[i] === item) {
                return true;
            }
        }

        return false;
    }
};
/**
 * PrimeUI Accordion widget
 */
$(function () {

    $.widget("primeui.puiaccordion", {

        options: {
            activeIndex: 0,
            multiple: false
        },

        _create: function () {
            if (this.options.multiple) {
                this.options.activeIndex = [];
            }

            var $this = this;
            this.element.addClass('pui-accordion ui-widget ui-helper-reset');

            this.element.children('h3').addClass('pui-accordion-header ui-helper-reset ui-state-default').each(function (i) {
                var header = $(this),
                    title = header.html(),
                    headerClass = (i == $this.options.activeIndex) ? 'ui-state-active ui-corner-top' : 'ui-corner-all',
                    iconClass = (i == $this.options.activeIndex) ? 'ui-icon ui-icon-triangle-1-s' : 'ui-icon ui-icon-triangle-1-e';

                header.addClass(headerClass).html('<span class="' + iconClass + '"></span><a href="#">' + title + '</a>');
            });

            this.element.children('div').each(function (i) {
                var content = $(this);
                content.addClass('pui-accordion-content ui-helper-reset ui-widget-content');

                if (i != $this.options.activeIndex) {
                    content.addClass('ui-helper-hidden');
                }
            });

            this.headers = this.element.children('.pui-accordion-header');
            this.panels = this.element.children('.pui-accordion-content');
            this.headers.children('a').disableSelection();

            this._bindEvents();
        },

        _bindEvents: function () {
            var $this = this;

            this.headers.mouseover(function () {
                var element = $(this);
                if (!element.hasClass('ui-state-active') && !element.hasClass('ui-state-disabled')) {
                    element.addClass('ui-state-hover');
                }
            }).mouseout(function () {
                    var element = $(this);
                    if (!element.hasClass('ui-state-active') && !element.hasClass('ui-state-disabled')) {
                        element.removeClass('ui-state-hover');
                    }
                }).click(function (e) {
                    var element = $(this);
                    if (!element.hasClass('ui-state-disabled')) {
                        var tabIndex = element.index() / 2;

                        if (element.hasClass('ui-state-active')) {
                            $this.unselect(tabIndex);
                        }
                        else {
                            $this.select(tabIndex);
                        }
                    }

                    e.preventDefault();
                });
        },

        /**
         *  Activates a tab with given index
         */
        select: function (index) {
            var panel = this.panels.eq(index);

            this._trigger('change', panel);

            //update state
            if (this.options.multiple)
                this._addToSelection(index);
            else
                this.options.activeIndex = index;

            this._show(panel);
        },

        /**
         *  Deactivates a tab with given index
         */
        unselect: function (index) {
            var panel = this.panels.eq(index),
                header = panel.prev();

            header.attr('aria-expanded', false).children('.ui-icon').removeClass('ui-icon-triangle-1-s').addClass('ui-icon-triangle-1-e');
            header.removeClass('ui-state-active ui-corner-top').addClass('ui-corner-all');
            panel.attr('aria-hidden', true).slideUp();

            this._removeFromSelection(index);
        },

        _show: function (panel) {
            //deactivate current
            if (!this.options.multiple) {
                var oldHeader = this.headers.filter('.ui-state-active');
                oldHeader.children('.ui-icon').removeClass('ui-icon-triangle-1-s').addClass('ui-icon-triangle-1-e');
                oldHeader.attr('aria-expanded', false).removeClass('ui-state-active ui-corner-top').addClass('ui-corner-all').next().attr('aria-hidden', true).slideUp();
            }

            //activate selected
            var newHeader = panel.prev();
            newHeader.attr('aria-expanded', true).addClass('ui-state-active ui-corner-top').removeClass('ui-state-hover ui-corner-all')
                .children('.ui-icon').removeClass('ui-icon-triangle-1-e').addClass('ui-icon-triangle-1-s');

            panel.attr('aria-hidden', false).slideDown('normal');
        },

        _addToSelection: function (nodeId) {
            this.options.activeIndex.push(nodeId);
        },

        _removeFromSelection: function (index) {
            this.options.activeIndex = $.grep(this.options.activeIndex, function (r) {
                return r != index;
            });
        }

    });
});
/**
 * PrimeUI autocomplete widget
 */
$(function () {

    $.widget("primeui.puiautocomplete", {

        options: {
            delay: 300,
            minQueryLength: 1,
            multiple: false,
            dropdown: false,
            scrollHeight: 200,
            forceSelection: false,
            effect: null,
            effectOptions: {},
            effectSpeed: 'normal',
            content: null,
            caseSensitive: false
        },

        _create: function () {
            this.element.puiinputtext();
            this.panel = $('<div class="pui-autocomplete-panel ui-widget-content ui-corner-all ui-helper-hidden pui-shadow"></div>').appendTo('body');

            if (this.options.multiple) {
                this.element.wrap('<ul class="pui-autocomplete-multiple ui-widget pui-inputtext ui-state-default ui-corner-all">' +
                    '<li class="pui-autocomplete-input-token"></li></ul>');
                this.inputContainer = this.element.parent();
                this.multiContainer = this.inputContainer.parent();
            }
            else {
                if (this.options.dropdown) {
                    this.dropdown = $('<button type="button" class="pui-button ui-widget ui-state-default ui-corner-right pui-button-icon-only">' +
                        '<span class="pui-button-icon-primary ui-icon ui-icon-triangle-1-s"></span><span class="pui-button-text">&nbsp;</span></button>')
                        .insertAfter(this.element);
                    this.element.removeClass('ui-corner-all').addClass('ui-corner-left');
                }
            }

            this._bindEvents();
        },

        _bindEvents: function () {
            var $this = this;

            this._bindKeyEvents();

            if (this.options.dropdown) {
                this.dropdown.on('hover.puiautocomplete', function () {
                    if (!$this.element.prop('disabled')) {
                        $this.dropdown.toggleClass('ui-state-hover');
                    }
                })
                    .on('mousedown.puiautocomplete', function () {
                        if (!$this.element.prop('disabled')) {
                            $this.dropdown.addClass('ui-state-active');
                        }
                    })
                    .on('mouseup.puiautocomplete', function () {
                        if (!$this.element.prop('disabled')) {
                            $this.dropdown.removeClass('ui-state-active');
                            $this.search('');
                            $this.element.focus();
                        }
                    })
                    .on('focus.puiautocomplete', function () {
                        $this.dropdown.addClass('ui-state-focus');
                    })
                    .on('blur.puiautocomplete', function () {
                        $this.dropdown.removeClass('ui-state-focus');
                    })
                    .on('keydown.puiautocomplete', function (e) {
                        var keyCode = $.ui.keyCode;

                        if (e.which == keyCode.ENTER || e.which == keyCode.NUMPAD_ENTER) {
                            $this.search('');
                            $this.input.focus();

                            e.preventDefault();
                        }
                    });
            }

            if (this.options.multiple) {
                this.multiContainer.on('hover.puiautocomplete', function () {
                    $(this).toggleClass('ui-state-hover');
                })
                    .on('click.puiautocomplete', function () {
                        $this.element.trigger('focus');
                    });

                this.element.on('focus.pui-autocomplete', function () {
                    $this.multiContainer.addClass('ui-state-focus');
                })
                    .on('blur.pui-autocomplete', function (e) {
                        $this.multiContainer.removeClass('ui-state-focus');
                    });
            }

            if (this.options.forceSelection) {
                this.currentItems = [this.element.val()];

                this.element.on('blur.puiautocomplete', function () {
                    var value = $(this).val(),
                        valid = false;

                    for (var i = 0; i < $this.currentItems.length; i++) {
                        if ($this.currentItems[i] === value) {
                            valid = true;
                            break;
                        }
                    }

                    if (!valid) {
                        $this.element.val('');
                    }
                });
            }

            $(document.body).bind('mousedown.puiautocomplete', function (e) {
                if ($this.panel.is(":hidden")) {
                    return;
                }

                if (e.target === $this.element.get(0)) {
                    return;
                }

                var offset = $this.panel.offset();
                if (e.pageX < offset.left ||
                    e.pageX > offset.left + $this.panel.width() ||
                    e.pageY < offset.top ||
                    e.pageY > offset.top + $this.panel.height()) {
                    $this.hide();
                }
            });

            $(window).bind('resize.' + this.element.id, function () {
                if ($this.panel.is(':visible')) {
                    $this._alignPanel();
                }
            });
        },

        _bindKeyEvents: function () {
            var $this = this;

            this.element.on('keyup.puiautocomplete',function (e) {
                var keyCode = $.ui.keyCode,
                    key = e.which,
                    shouldSearch = true;

                if (key == keyCode.UP
                    || key == keyCode.LEFT
                    || key == keyCode.DOWN
                    || key == keyCode.RIGHT
                    || key == keyCode.TAB
                    || key == keyCode.SHIFT
                    || key == keyCode.ENTER
                    || key == keyCode.NUMPAD_ENTER) {
                    shouldSearch = false;
                }

                if (shouldSearch) {
                    var value = $this.element.val();

                    if (!value.length) {
                        $this.hide();
                    }

                    if (value.length >= $this.options.minQueryLength) {
                        if ($this.timeout) {
                            clearTimeout($this.timeout);
                        }

                        $this.timeout = setTimeout(function () {
                                $this.search(value);
                            },
                            $this.options.delay);
                    }
                }

            }).on('keydown.puiautocomplete', function (e) {
                    if ($this.panel.is(':visible')) {
                        var keyCode = $.ui.keyCode,
                            highlightedItem = $this.items.filter('.ui-state-highlight');

                        switch (e.which) {
                            case keyCode.UP:
                            case keyCode.LEFT:
                                var prev = highlightedItem.prev();

                                if (prev.length == 1) {
                                    highlightedItem.removeClass('ui-state-highlight');
                                    prev.addClass('ui-state-highlight');

                                    if ($this.options.scrollHeight) {
                                        PUI.scrollInView($this.panel, prev);
                                    }
                                }

                                e.preventDefault();
                                break;

                            case keyCode.DOWN:
                            case keyCode.RIGHT:
                                var next = highlightedItem.next();

                                if (next.length == 1) {
                                    highlightedItem.removeClass('ui-state-highlight');
                                    next.addClass('ui-state-highlight');

                                    if ($this.options.scrollHeight) {
                                        PUI.scrollInView($this.panel, next);
                                    }
                                }

                                e.preventDefault();
                                break;

                            case keyCode.ENTER:
                            case keyCode.NUMPAD_ENTER:
                                highlightedItem.trigger('click');

                                e.preventDefault();
                                break;

                            case keyCode.ALT:
                            case 224:
                                break;

                            case keyCode.TAB:
                                highlightedItem.trigger('click');
                                $this.hide();
                                break;
                        }
                    }

                });
        },

        _bindDynamicEvents: function () {
            var $this = this;

            this.items.on('mouseover.puiautocomplete', function () {
                var item = $(this);

                if (!item.hasClass('ui-state-highlight')) {
                    $this.items.filter('.ui-state-highlight').removeClass('ui-state-highlight');
                    item.addClass('ui-state-highlight');
                }
            })
                .on('click.puiautocomplete', function (event) {
                    var item = $(this);

                    if ($this.options.multiple) {
                        var tokenMarkup = '<li class="pui-autocomplete-token ui-state-active ui-corner-all ui-helper-hidden">';
                        tokenMarkup += '<span class="pui-autocomplete-token-icon ui-icon ui-icon-close" />';
                        tokenMarkup += '<span class="pui-autocomplete-token-label">' + item.data('label') + '</span></li>';

                        $(tokenMarkup).data(item.data())
                            .insertBefore($this.inputContainer).fadeIn()
                            .children('.pui-autocomplete-token-icon').on('click.pui-autocomplete', function (e) {
                                var token = $(this).parent();
                                $this._removeItem(token);
                                $this._trigger('unselect', e, token);
                            });

                        $this.element.val('').trigger('focus');
                    }
                    else {
                        $this.element.val(item.data('label')).focus();
                    }

                    $this._trigger('select', event, item);
                    $this.hide();
                });
        },

        search: function (q) {
            this.query = this.options.caseSensitive ? q : q.toLowerCase();

            var request = {
                query: this.query
            };

            if (this.options.completeSource) {
                if ($.isArray(this.options.completeSource)) {
                    var sourceArr = this.options.completeSource,
                        data = [],
                        emptyQuery = ($.trim(q) === '');

                    for (var i = 0; i < sourceArr.length; i++) {
                        var item = sourceArr[i],
                            itemLabel = item.label || item;

                        if (!this.options.caseSensitive) {
                            itemLabel = itemLabel.toLowerCase();
                        }

                        if (emptyQuery || itemLabel.indexOf(this.query) === 0) {
                            data.push({label: sourceArr[i], value: item});
                        }
                    }

                    this._handleData(data);
                }
                else {
                    this.options.completeSource.call(this, request, this._handleData);
                }
            }
        },

        _handleData: function (data) {
            var $this = this;
            this.panel.html('');
            this.listContainer = $('<ul class="pui-autocomplete-items pui-autocomplete-list ui-widget-content ui-widget ui-corner-all ui-helper-reset"></ul>').appendTo(this.panel);

            for (var i = 0; i < data.length; i++) {
                var item = $('<li class="pui-autocomplete-item pui-autocomplete-list-item ui-corner-all"></li>');
                item.data(data[i]);

                if (this.options.content)
                    item.html(this.options.content.call(this, data[i]));
                else
                    item.text(data[i].label);

                this.listContainer.append(item);
            }

            this.items = this.listContainer.children('.pui-autocomplete-item');

            this._bindDynamicEvents();

            if (this.items.length > 0) {
                var firstItem = $this.items.eq(0),
                    hidden = this.panel.is(':hidden');
                firstItem.addClass('ui-state-highlight');

                if ($this.query.length > 0 && !$this.options.content) {
                    $this.items.each(function () {
                        var item = $(this),
                            text = item.html(),
                            re = new RegExp(PUI.escapeRegExp($this.query), 'gi'),
                            highlighedText = text.replace(re, '<span class="pui-autocomplete-query">$&</span>');

                        item.html(highlighedText);
                    });
                }

                if (this.options.forceSelection) {
                    this.currentItems = [];
                    $.each(data, function (i, item) {
                        $this.currentItems.push(item.label);
                    });
                }

                //adjust height
                if ($this.options.scrollHeight) {
                    var heightConstraint = hidden ? $this.panel.height() : $this.panel.children().height();

                    if (heightConstraint > $this.options.scrollHeight)
                        $this.panel.height($this.options.scrollHeight);
                    else
                        $this.panel.css('height', 'auto');

                }

                if (hidden) {
                    $this.show();
                }
                else {
                    $this._alignPanel();
                }
            }
            else {
                this.panel.hide();
            }
        },

        show: function () {
            this._alignPanel();

            if (this.options.effect)
                this.panel.show(this.options.effect, {}, this.options.effectSpeed);
            else
                this.panel.show();
        },

        hide: function () {
            this.panel.hide();
            this.panel.css('height', 'auto');
        },

        _removeItem: function (item) {
            item.fadeOut('fast', function () {
                var token = $(this);

                token.remove();
            });
        },

        _alignPanel: function () {
            var panelWidth = null;

            if (this.options.multiple) {
                panelWidth = this.multiContainer.innerWidth() - (this.element.position().left - this.multiContainer.position().left);
            }
            else {
                if (this.panel.is(':visible')) {
                    panelWidth = this.panel.children('.pui-autocomplete-items').outerWidth();
                }
                else {
                    this.panel.css({'visibility': 'hidden', 'display': 'block'});
                    panelWidth = this.panel.children('.pui-autocomplete-items').outerWidth();
                    this.panel.css({'visibility': 'visible', 'display': 'none'});
                }

                var inputWidth = this.element.outerWidth();
                if (panelWidth < inputWidth) {
                    panelWidth = inputWidth;
                }
            }

            this.panel.css({
                'left': '',
                'top': '',
                'width': panelWidth,
                'z-index': ++PUI.zindex
            })
                .position({
                    my: 'left top', at: 'left bottom', of: this.element
                });
        }
    });

});
/**
 * PrimeFaces Button Widget
 */
$(function () {

    $.widget("primeui.puibutton", {

        options: {
            value: null,
            icon: null,
            iconPos: 'left',
            click: null
        },

        _create: function () {
            var element = this.element,
                elementText = element.text(),
                value = this.options.value || (elementText === '' ? 'pui-button' : elementText),
                disabled = element.prop('disabled'),
                styleClass = null;

            if (this.options.icon)
                styleClass = (value === 'pui-button') ? 'pui-button-icon-only' : 'pui-button-text-icon-' + this.options.iconPos;
            else
                styleClass = 'pui-button-text-only';

            if (disabled) {
                styleClass += ' ui-state-disabled';
            }

            this.element.addClass('pui-button ui-widget ui-state-default ui-corner-all ' + styleClass).text('');

            if (this.options.icon) {
                this.element.append('<span class="pui-button-icon-' + this.options.iconPos + ' ui-icon ' + this.options.icon + '" />');
            }

            this.element.append('<span class="pui-button-text">' + value + '</span>');

            //aria
            element.attr('role', 'button').attr('aria-disabled', disabled);

            if (!disabled) {
                this._bindEvents();
            }
        },

        _bindEvents: function () {
            var element = this.element,
                $this = this;

            element.on('mouseover.puibutton',function () {
                if (!element.prop('disabled')) {
                    element.addClass('ui-state-hover');
                }
            }).on('mouseout.puibutton',function () {
                    $(this).removeClass('ui-state-active ui-state-hover');
                }).on('mousedown.puibutton',function () {
                    if (!element.hasClass('ui-state-disabled')) {
                        element.addClass('ui-state-active').removeClass('ui-state-hover');
                    }
                }).on('mouseup.puibutton',function (e) {
                    element.removeClass('ui-state-active').addClass('ui-state-hover');

                    $this._trigger('click', e);
                }).on('focus.puibutton',function () {
                    element.addClass('ui-state-focus');
                }).on('blur.puibutton',function () {
                    element.removeClass('ui-state-focus');
                }).on('keydown.puibutton',function (e) {
                    if (e.keyCode == $.ui.keyCode.SPACE || e.keyCode == $.ui.keyCode.ENTER || e.keyCode == $.ui.keyCode.NUMPAD_ENTER) {
                        element.addClass('ui-state-active');
                    }
                }).on('keyup.puibutton', function () {
                    element.removeClass('ui-state-active');
                });

            return this;
        },

        _unbindEvents: function () {
            this.element.off('mouseover.puibutton mouseout.puibutton mousedown.puibutton mouseup.puibutton focus.puibutton blur.puibutton keydown.puibutton keyup.puibutton');
        },

        disable: function () {
            this._unbindEvents();

            this.element.attr({
                'disabled': 'disabled',
                'aria-disabled': true
            }).addClass('ui-state-disabled');
        },

        enable: function () {
            this._bindEvents();

            this.element.removeAttr('disabled').attr('aria-disabled', false).removeClass('ui-state-disabled');
        }
    });
});
/**
 * PrimeUI checkbox widget
 */
$(function () {

    $.widget("primeui.puicheckbox", {

        _create: function () {
            this.element.wrap('<div class="pui-chkbox ui-widget"><div class="ui-helper-hidden-accessible"></div></div>');
            this.container = this.element.parent().parent();
            this.box = $('<div class="pui-chkbox-box ui-widget ui-corner-all ui-state-default">').appendTo(this.container);
            this.icon = $('<span class="pui-chkbox-icon pui-c"></span>').appendTo(this.box);
            this.disabled = this.element.prop('disabled');
            this.label = $('label[for="' + this.element.attr('id') + '"]');

            if (this.element.prop('checked')) {
                this.box.addClass('ui-state-active');
                this.icon.addClass('ui-icon ui-icon-check');
            }

            if (this.disabled) {
                this.box.addClass('ui-state-disabled');
            } else {
                this._bindEvents();
            }
        },

        _bindEvents: function () {
            var $this = this;

            this.box.on('mouseover.puicheckbox', function () {
                if (!$this.isChecked())
                    $this.box.addClass('ui-state-hover');
            })
                .on('mouseout.puicheckbox', function () {
                    $this.box.removeClass('ui-state-hover');
                })
                .on('click.puicheckbox', function () {
                    $this.toggle();
                });

            this.element.focus(function () {
                if ($this.isChecked()) {
                    $this.box.removeClass('ui-state-active');
                }

                $this.box.addClass('ui-state-focus');
            })
                .blur(function () {
                    if ($this.isChecked()) {
                        $this.box.addClass('ui-state-active');
                    }

                    $this.box.removeClass('ui-state-focus');
                })
                .keydown(function (e) {
                    var keyCode = $.ui.keyCode;
                    if (e.which == keyCode.SPACE) {
                        e.preventDefault();
                    }
                })
                .keyup(function (e) {
                    var keyCode = $.ui.keyCode;
                    if (e.which == keyCode.SPACE) {
                        $this.toggle(true);

                        e.preventDefault();
                    }
                });

            this.label.on('click.puicheckbox', function (e) {
                $this.toggle();
                e.preventDefault();
            });
        },

        toggle: function (keypress) {
            if (this.isChecked()) {
                this.uncheck(keypress);
            } else {
                this.check(keypress);
            }

            this._trigger('change', null, this.isChecked());
        },

        isChecked: function () {
            return this.element.prop('checked');
        },

        check: function (activate, silent) {
            if (!this.isChecked()) {
                this.element.prop('checked', true);
                this.icon.addClass('ui-icon ui-icon-check');

                if (!activate) {
                    this.box.addClass('ui-state-active');
                }

                if (!silent) {
                    this.element.trigger('change');
                }
            }
        },

        uncheck: function () {
            if (this.isChecked()) {
                this.element.prop('checked', false);
                this.box.removeClass('ui-state-active');
                this.icon.removeClass('ui-icon ui-icon-check');

                this.element.trigger('change');
            }
        }
    });

});
/**
 * PrimeUI Datatable Widget
 */
$(function () {

    $.widget("primeui.puidatatable", {

        options: {
            columns: null,
            datasource: null,
            paginator: null,
            selectionMode: null,
            rowSelect: null,
            rowUnselect: null,
            caption: null,
            sortField: null,
            sortOrder: null
        },

        _create: function () {
            this.id = this.element.attr('id');
            if (!this.id) {
                this.id = this.element.uniqueId().attr('id');
            }

            this.element.addClass('pui-datatable ui-widget');
            this.tableWrapper = $('<div class="pui-datatable-tablewrapper" />').appendTo(this.element);
            this.table = $('<table><thead></thead><tbody></tbody></table>').appendTo(this.tableWrapper);
            this.thead = this.table.children('thead');
            this.tbody = this.table.children('tbody').addClass('pui-datatable-data');

            if (this.options.datasource) {
                if ($.isArray(this.options.datasource)) {
                    this.data = this.options.datasource;
                    this._initialize();
                }
                else if ($.type(this.options.datasource) === 'function') {
                    if (this.options.lazy)
                        this.options.datasource.call(this, this._onDataInit, {first: 0, sortField: this.options.sortField, sortorder: this.options.sortOrder});
                    else
                        this.options.datasource.call(this, this._onDataInit);
                }
            }
        },

        _initialize: function () {
            var $this = this;

            if (this.options.columns) {
                $.each(this.options.columns, function (i, col) {
                    var header = $('<th class="ui-state-default"></th>').data('field', col.field).appendTo($this.thead);

                    if (col.headerText) {
                        header.text(col.headerText);
                    }

                    if (col.sortable) {
                        header.addClass('pui-sortable-column')
                            .data('order', 0)
                            .append('<span class="pui-sortable-column-icon ui-icon ui-icon-carat-2-n-s"></span>');
                    }
                });
            }

            if (this.options.caption) {
                this.table.prepend('<caption class="pui-datatable-caption ui-widget-header">' + this.options.caption + '</caption>');
            }

            if (this.options.paginator) {
                this.options.paginator.paginate = function (event, state) {
                    $this.paginate();
                };

                this.options.paginator.totalRecords = this.options.paginator.totalRecords || this.data.length;
                this.paginator = $('<div></div>').insertAfter(this.tableWrapper).puipaginator(this.options.paginator);
            }

            if (this._isSortingEnabled()) {
                this._initSorting();
            }

            if (this.options.selectionMode) {
                this._initSelection();
            }

            this._renderData();
        },

        _onDataInit: function (data) {
            this.data = data;
            if (!this.data) {
                this.data = [];
            }

            this._initialize();
        },

        _onDataUpdate: function (data) {
            this.data = data;
            if (!this.data) {
                this.data = [];
            }

            this._renderData();
        },

        _onLazyLoad: function (data) {
            this.data = data;
            if (!this.data) {
                this.data = [];
            }

            this._renderData();
        },

        _initSorting: function () {
            var $this = this,
                sortableColumns = this.thead.children('th.pui-sortable-column');

            sortableColumns.on('mouseover.puidatatable', function () {
                var column = $(this);

                if (!column.hasClass('ui-state-active'))
                    column.addClass('ui-state-hover');
            })
                .on('mouseout.puidatatable', function () {
                    var column = $(this);

                    if (!column.hasClass('ui-state-active'))
                        column.removeClass('ui-state-hover');
                })
                .on('click.puidatatable', function () {
                    var column = $(this),
                        sortField = column.data('field'),
                        order = column.data('order'),
                        sortOrder = (order === 0) ? 1 : (order * -1),
                        sortIcon = column.children('.pui-sortable-column-icon');

                    //clean previous sort state
                    column.siblings().filter('.ui-state-active').data('order', 0).removeClass('ui-state-active').children('span.pui-sortable-column-icon')
                        .removeClass('ui-icon-triangle-1-n ui-icon-triangle-1-s');

                    //update state
                    $this.options.sortField = sortField;
                    $this.options.sortOrder = sortOrder;

                    $this.sort(sortField, sortOrder);

                    column.data('order', sortOrder).removeClass('ui-state-hover').addClass('ui-state-active');
                    if (sortOrder === -1)
                        sortIcon.removeClass('ui-icon-triangle-1-n').addClass('ui-icon-triangle-1-s');
                    else if (sortOrder === 1)
                        sortIcon.removeClass('ui-icon-triangle-1-s').addClass('ui-icon-triangle-1-n');
                });
        },

        paginate: function () {
            if (this.options.lazy) {
                if (this.options.selectionMode) {
                    this.selection = [];
                }
                this.options.datasource.call(this, this._onLazyLoad, this._createStateMeta());
            }
            else {
                this._renderData();
            }
        },

        sort: function (field, order) {
            if (this.options.selectionMode) {
                this.selection = [];
            }

            if (this.options.lazy) {
                this.options.datasource.call(this, this._onLazyLoad, this._createStateMeta());
            }
            else {
                this.data.sort(function (data1, data2) {
                    var value1 = data1[field],
                        value2 = data2[field],
                        result = (value1 < value2) ? -1 : (value1 > value2) ? 1 : 0;

                    return (order * result);
                });

                if (this.options.selectionMode) {
                    this.selection = [];
                }

                if (this.paginator) {
                    this.paginator.puipaginator('option', 'page', 0);
                }

                this._renderData();
            }
        },

        sortByField: function (a, b) {
            var aName = a.name.toLowerCase();
            var bName = b.name.toLowerCase();
            return ((aName < bName) ? -1 : ((aName > bName) ? 1 : 0));
        },

        _renderData: function () {
            if (this.data) {
                this.tbody.html('');

                var first = this.options.lazy ? 0 : this._getFirst(),
                    rows = this._getRows();

                for (var i = first; i < (first + rows); i++) {
                    var rowData = this.data[i];

                    if (rowData) {
                        var row = $('<tr class="ui-widget-content" />').appendTo(this.tbody),
                            zebraStyle = (i % 2 === 0) ? 'pui-datatable-even' : 'pui-datatable-odd';

                        row.addClass(zebraStyle);

                        if (this.options.selectionMode && PUI.inArray(this.selection, i)) {
                            row.addClass("ui-state-highlight");
                        }

                        for (var j = 0; j < this.options.columns.length; j++) {
                            var column = $('<td />').appendTo(row);

                            if (this.options.columns[j].content) {
                                var content = this.options.columns[j].content.call(this, rowData);
                                if ($.type(content) === 'string')
                                    column.html(content);
                                else
                                    column.append(content);
                            }
                            else {
                                column.text(rowData[this.options.columns[j].field]);
                            }
                        }
                    }
                }
            }
        },

        _getFirst: function () {
            if (this.paginator) {
                var page = this.paginator.puipaginator('option', 'page'),
                    rows = this.paginator.puipaginator('option', 'rows');

                return (page * rows);
            }
            else {
                return 0;
            }
        },

        _getRows: function () {
            return this.paginator ? this.paginator.puipaginator('option', 'rows') : this.data.length;
        },

        _isSortingEnabled: function () {
            var cols = this.options.columns;
            if (cols) {
                for (var i = 0; i < cols.length; i++) {
                    if (cols[i].sortable) {
                        return true;
                    }
                }
            }

            return false;
        },

        _initSelection: function () {
            var $this = this;
            this.selection = [];
            this.rowSelector = '#' + this.id + ' tbody.pui-datatable-data > tr.ui-widget-content:not(.ui-datatable-empty-message)';

            //shift key based range selection
            if (this._isMultipleSelection()) {
                this.originRowIndex = 0;
                this.cursorIndex = null;
            }

            $(document).off('mouseover.puidatatable mouseout.puidatatable click.puidatatable', this.rowSelector)
                .on('mouseover.datatable', this.rowSelector, null, function () {
                    var element = $(this);

                    if (!element.hasClass('ui-state-highlight')) {
                        element.addClass('ui-state-hover');
                    }
                })
                .on('mouseout.datatable', this.rowSelector, null, function () {
                    var element = $(this);

                    if (!element.hasClass('ui-state-highlight')) {
                        element.removeClass('ui-state-hover');
                    }
                })
                .on('click.datatable', this.rowSelector, null, function (e) {
                    $this._onRowClick(e, this);
                });
        },

        _onRowClick: function (event, rowElement) {
            if (!$(event.target).is(':input,:button,a')) {
                var row = $(rowElement),
                    selected = row.hasClass('ui-state-highlight'),
                    metaKey = event.metaKey || event.ctrlKey,
                    shiftKey = event.shiftKey;

                //unselect a selected row if metakey is on
                if (selected && metaKey) {
                    this.unselectRow(row);
                }
                else {
                    //unselect previous selection if this is single selection or multiple one with no keys
                    if (this._isSingleSelection() || (this._isMultipleSelection() && !metaKey && !shiftKey)) {
                        this.unselectAllRows();
                    }

                    this.selectRow(row, false, event);
                }

                PUI.clearSelection();
            }
        },

        _isSingleSelection: function () {
            return this.options.selectionMode === 'single';
        },

        _isMultipleSelection: function () {
            return this.options.selectionMode === 'multiple';
        },

        unselectAllRows: function () {
            this.tbody.children('tr.ui-state-highlight').removeClass('ui-state-highlight').attr('aria-selected', false);
            this.selection = [];
        },

        unselectRow: function (row, silent) {
            var rowIndex = this._getRowIndex(row);
            row.removeClass('ui-state-highlight').attr('aria-selected', false);

            this._removeSelection(rowIndex);

            if (!silent) {
                this._trigger('rowUnselect', null, this.data[rowIndex]);
            }
        },

        selectRow: function (row, silent, event) {
            var rowIndex = this._getRowIndex(row);
            row.removeClass('ui-state-hover').addClass('ui-state-highlight').attr('aria-selected', true);

            this._addSelection(rowIndex);

            if (!silent) {
                this._trigger('rowSelect', event, this.data[rowIndex]);
            }
        },

        getSelection: function () {
            var selections = [];
            for (var i = 0; i < this.selection.length; i++) {
                selections.push(this.data[this.selection[i]]);
            }

            return selections;
        },

        _removeSelection: function (rowIndex) {
            this.selection = $.grep(this.selection, function (value) {
                return value !== rowIndex;
            });
        },

        _addSelection: function (rowIndex) {
            if (!this._isSelected(rowIndex)) {
                this.selection.push(rowIndex);
            }
        },

        _isSelected: function (rowIndex) {
            return PUI.inArray(this.selection, rowIndex);
        },

        _getRowIndex: function (row) {
            var index = row.index();

            return this.options.paginator ? this._getFirst() + index : index;
        },

        _createStateMeta: function () {
            var state = {
                first: this._getFirst(),
                rows: this._getRows(),
                sortField: this.options.sortField,
                sortOrder: this.options.sortOrder
            };

            return state;
        },

        _updateDatasource: function (datasource) {
            this.options.datasource = datasource;

            this.reset();

            if ($.isArray(this.options.datasource)) {
                this.data = this.options.datasource;
                this._renderData();
            }
            else if ($.type(this.options.datasource) === 'function') {
                if (this.options.lazy)
                    this.options.datasource.call(this, this._onDataUpdate, {first: 0, sortField: this.options.sortField, sortorder: this.options.sortOrder});
                else
                    this.options.datasource.call(this, this._onDataUpdate);
            }
        },

        _setOption: function (key, value) {
            if (key === 'datasource') {
                this._updateDatasource(value);
            }
            else {
                $.Widget.prototype._setOption.apply(this, arguments);
            }
        },

        reset: function () {
            if (this.options.selectionMode) {
                this.selection = [];
            }

            if (this.paginator) {
                this.paginator.puipaginator('setPage', 0, true);
            }

            this.thead.children('th.pui-sortable-column').data('order', 0).filter('.ui-state-active').removeClass('ui-state-active')
                .children('span.pui-sortable-column-icon').removeClass('ui-icon-triangle-1-n ui-icon-triangle-1-s');
        }
    });
});
/**
 * PrimeUI Dialog Widget
 */
$(function () {

    $.widget("primeui.puidialog", {

        options: {
            draggable: true,
            resizable: true,
            location: 'center',
            minWidth: 150,
            minHeight: 25,
            height: 'auto',
            width: '300px',
            visible: false,
            modal: false,
            showEffect: null,
            hideEffect: null,
            effectOptions: {},
            effectSpeed: 'normal',
            closeOnEscape: true,
            rtl: false,
            closable: true,
            minimizable: false,
            maximizable: false,
            appendTo: null,
            buttons: null
        },

        _create: function () {
            //container
            this.element.addClass('pui-dialog ui-widget ui-widget-content ui-helper-hidden ui-corner-all pui-shadow')
                .contents().wrapAll('<div class="pui-dialog-content ui-widget-content" />');

            //header
            this.element.prepend('<div class="pui-dialog-titlebar ui-widget-header ui-helper-clearfix ui-corner-top">'
                    + '<span id="' + this.element.attr('id') + '_label" class="pui-dialog-title">' + this.element.attr('title') + '</span>')
                .removeAttr('title');

            //footer
            if (this.options.buttons) {
                this.footer = $('<div class="pui-dialog-buttonpane ui-widget-content ui-helper-clearfix"></div>').appendTo(this.element);
                for (var i = 0; i < this.options.buttons.length; i++) {
                    var buttonMeta = this.options.buttons[i],
                        button = $('<button type="button"></button>').appendTo(this.footer);
                    if (buttonMeta.text) {
                        button.text(buttonMeta.text);
                    }

                    button.puibutton(buttonMeta);
                }
            }

            if (this.options.rtl) {
                this.element.addClass('pui-dialog-rtl');
            }

            //elements
            this.content = this.element.children('.pui-dialog-content');
            this.titlebar = this.element.children('.pui-dialog-titlebar');

            if (this.options.closable) {
                this._renderHeaderIcon('pui-dialog-titlebar-close', 'ui-icon-close');
            }

            if (this.options.minimizable) {
                this._renderHeaderIcon('pui-dialog-titlebar-maximize', 'ui-icon-extlink');
            }

            if (this.options.minimizable) {
                this._renderHeaderIcon('pui-dialog-titlebar-minimize', 'ui-icon-minus');
            }

            //icons
            this.icons = this.titlebar.children('.pui-dialog-titlebar-icon');
            this.closeIcon = this.titlebar.children('.pui-dialog-titlebar-close');
            this.minimizeIcon = this.titlebar.children('.pui-dialog-titlebar-minimize');
            this.maximizeIcon = this.titlebar.children('.pui-dialog-titlebar-maximize');

            this.blockEvents = 'focus.puidialog mousedown.puidialog mouseup.puidialog keydown.puidialog keyup.puidialog';
            this.parent = this.element.parent();

            //size
            this.element.css({'width': this.options.width, 'height': 'auto'});
            this.content.height(this.options.height);

            //events
            this._bindEvents();

            if (this.options.draggable) {
                this._setupDraggable();
            }

            if (this.options.resizable) {
                this._setupResizable();
            }

            if (this.options.appendTo) {
                this.element.appendTo(this.options.appendTo);
            }

            //docking zone
            if ($(document.body).children('.pui-dialog-docking-zone').length == 0) {
                $(document.body).append('<div class="pui-dialog-docking-zone"></div>')
            }

            //aria
            this._applyARIA();

            if (this.options.visible) {
                this.show();
            }
        },

        _renderHeaderIcon: function (styleClass, icon) {
            this.titlebar.append('<a class="pui-dialog-titlebar-icon ' + styleClass + ' ui-corner-all" href="#" role="button">'
                + '<span class="ui-icon ' + icon + '"></span></a>');
        },

        _enableModality: function () {
            var $this = this,
                doc = $(document);

            this.modality = $('<div id="' + this.element.attr('id') + '_modal" class="ui-widget-overlay"></div>').appendTo(document.body)
                .css({
                    'width': doc.width(),
                    'height': doc.height(),
                    'z-index': this.element.css('z-index') - 1
                });

            //Disable tabbing out of modal dialog and stop events from targets outside of dialog
            doc.bind('keydown.puidialog',
                function (event) {
                    if (event.keyCode == $.ui.keyCode.TAB) {
                        var tabbables = $this.content.find(':tabbable'),
                            first = tabbables.filter(':first'),
                            last = tabbables.filter(':last');

                        if (event.target === last[0] && !event.shiftKey) {
                            first.focus(1);
                            return false;
                        }
                        else if (event.target === first[0] && event.shiftKey) {
                            last.focus(1);
                            return false;
                        }
                    }
                })
                .bind(this.blockEvents, function (event) {
                    if ($(event.target).zIndex() < $this.element.zIndex()) {
                        return false;
                    }
                });
        },

        _disableModality: function () {
            this.modality.remove();
            this.modality = null;
            $(document).unbind(this.blockEvents).unbind('keydown.dialog');
        },

        show: function () {
            if (this.element.is(':visible')) {
                return;
            }

            if (!this.positionInitialized) {
                this._initPosition();
            }

            this._trigger('beforeShow', null);

            if (this.options.showEffect) {
                var $this = this;

                this.element.show(this.options.showEffect, this.options.effectOptions, this.options.effectSpeed, function () {
                    $this._postShow();
                });
            }
            else {
                this.element.show();

                this._postShow();
            }

            this._moveToTop();

            if (this.options.modal) {
                this._enableModality();
            }
        },

        _postShow: function () {
            //execute user defined callback
            this._trigger('afterShow', null);

            this.element.attr({
                'aria-hidden': false, 'aria-live': 'polite'
            });

            this._applyFocus();
        },

        hide: function () {
            if (this.element.is(':hidden')) {
                return;
            }

            this._trigger('beforeHide', null);

            if (this.options.hideEffect) {
                var _self = this;

                this.element.hide(this.options.hideEffect, this.options.effectOptions, this.options.effectSpeed, function () {
                    _self._postHide();
                });
            }
            else {
                this.element.hide();

                this._postHide();
            }

            if (this.options.modal) {
                this._disableModality();
            }
        },

        _postHide: function () {
            //execute user defined callback
            this._trigger('afterHide', null);

            this.element.attr({
                'aria-hidden': true, 'aria-live': 'off'
            });
        },

        _applyFocus: function () {
            this.element.find(':not(:submit):not(:button):input:visible:enabled:first').focus();
        },

        _bindEvents: function () {
            var $this = this;

            this.icons.mouseover(function () {
                $(this).addClass('ui-state-hover');
            }).mouseout(function () {
                    $(this).removeClass('ui-state-hover');
                });

            this.closeIcon.on('click.puidialog', function (e) {
                $this.hide();
                e.preventDefault();
            });

            this.maximizeIcon.click(function (e) {
                $this.toggleMaximize();
                e.preventDefault();
            });

            this.minimizeIcon.click(function (e) {
                $this.toggleMinimize();
                e.preventDefault();
            });

            if (this.options.closeOnEscape) {
                $(document).on('keydown.dialog_' + this.element.attr('id'), function (e) {
                    var keyCode = $.ui.keyCode,
                        active = parseInt($this.element.css('z-index')) === PUI.zindex;

                    if (e.which === keyCode.ESCAPE && $this.element.is(':visible') && active) {
                        $this.hide();
                    }
                    ;
                });
            }

            if (this.options.modal) {
                $(window).on('resize.puidialog', function () {
                    $(document.body).children('.ui-widget-overlay').css({
                        'width': $(document).width(), 'height': $(document).height()
                    });
                });
            }
        },

        _setupDraggable: function () {
            this.element.draggable({
                cancel: '.pui-dialog-content, .pui-dialog-titlebar-close',
                handle: '.pui-dialog-titlebar',
                containment: 'document'
            });
        },

        _setupResizable: function () {
            this.element.resizable({
                minWidth: this.options.minWidth,
                minHeight: this.options.minHeight,
                alsoResize: this.content,
                containment: 'document'
            });

            this.resizers = this.element.children('.ui-resizable-handle');
        },

        _initPosition: function () {
            //reset
            this.element.css({left: 0, top: 0});

            if (/(center|left|top|right|bottom)/.test(this.options.location)) {
                this.options.location = this.options.location.replace(',', ' ');

                this.element.position({
                    my: 'center', at: this.options.location, collision: 'fit', of: window
                    //make sure dialog stays in viewport
                    , using: function (pos) {
                        var l = pos.left < 0 ? 0 : pos.left,
                            t = pos.top < 0 ? 0 : pos.top;

                        $(this).css({
                            left: l, top: t
                        });
                    }
                });
            }
            else {
                var coords = this.options.position.split(','),
                    x = $.trim(coords[0]),
                    y = $.trim(coords[1]);

                this.element.offset({
                    left: x, top: y
                });
            }

            this.positionInitialized = true;
        },

        _moveToTop: function () {
            this.element.css('z-index', ++PUI.zindex);
        },

        toggleMaximize: function () {
            if (this.minimized) {
                this.toggleMinimize();
            }

            if (this.maximized) {
                this.element.removeClass('pui-dialog-maximized');
                this._restoreState();

                this.maximizeIcon.removeClass('ui-state-hover').children('.ui-icon').removeClass('ui-icon-newwin').addClass('ui-icon-extlink');
                this.maximized = false;
            }
            else {
                this._saveState();

                var win = $(window);

                this.element.addClass('pui-dialog-maximized').css({
                    'width': win.width() - 6, 'height': win.height()
                }).offset({
                        top: win.scrollTop(), left: win.scrollLeft()
                    });

                //maximize content
                this.content.css({
                    width: 'auto',
                    height: 'auto'
                });

                this.maximizeIcon.removeClass('ui-state-hover').children('.ui-icon').removeClass('ui-icon-extlink').addClass('ui-icon-newwin');
                this.maximized = true;
                this._trigger('maximize');
            }
        },

        toggleMinimize: function () {
            var animate = true,
                dockingZone = $(document.body).children('.pui-dialog-docking-zone');

            if (this.maximized) {
                this.toggleMaximize();
                animate = false;
            }

            var $this = this;

            if (this.minimized) {
                this.element.appendTo(this.parent).removeClass('pui-dialog-minimized').css({'position': 'fixed', 'float': 'none'});
                this._restoreState();
                this.content.show();
                this.minimizeIcon.removeClass('ui-state-hover').children('.ui-icon').removeClass('ui-icon-plus').addClass('ui-icon-minus');
                this.minimized = false;

                if (this.options.resizable) {
                    this.resizers.show();
                }

                if (this.footer) {
                    this.footer.show();
                }
            }
            else {
                this._saveState();

                if (animate) {
                    this.element.effect('transfer', {
                            to: dockingZone, className: 'pui-dialog-minimizing'
                        }, 500,
                        function () {
                            $this._dock(dockingZone);
                            $this.element.addClass('pui-dialog-minimized');
                        });
                }
                else {
                    this._dock(dockingZone);
                }
            }
        },

        _dock: function (zone) {
            this.element.appendTo(zone).css('position', 'static');
            this.element.css({'height': 'auto', 'width': 'auto', 'float': 'left'});
            this.content.hide();
            this.minimizeIcon.removeClass('ui-state-hover').children('.ui-icon').removeClass('ui-icon-minus').addClass('ui-icon-plus');
            this.minimized = true;

            if (this.options.resizable) {
                this.resizers.hide();
            }

            if (this.footer) {
                this.footer.hide();
            }

            zone.css('z-index', ++PUI.zindex);

            this._trigger('minimize');
        },

        _saveState: function () {
            this.state = {
                width: this.element.width(), height: this.element.height()
            };

            var win = $(window);
            this.state.offset = this.element.offset();
            this.state.windowScrollLeft = win.scrollLeft();
            this.state.windowScrollTop = win.scrollTop();
        },

        _restoreState: function () {
            this.element.width(this.state.width).height(this.state.height);

            var win = $(window);
            this.element.offset({
                top: this.state.offset.top + (win.scrollTop() - this.state.windowScrollTop), left: this.state.offset.left + (win.scrollLeft() - this.state.windowScrollLeft)
            });
        },

        _applyARIA: function () {
            this.element.attr({
                'role': 'dialog', 'aria-labelledby': this.element.attr('id') + '_title', 'aria-hidden': !this.options.visible
            });

            this.titlebar.children('a.pui-dialog-titlebar-icon').attr('role', 'button');
        }
    });
});
/**
 * PrimeUI dropdown widget
 */
$(function () {

    $.widget("primeui.puidropdown", {

        options: {
            effect: 'fade',
            effectSpeed: 'normal',
            filter: false,
            filterMatchMode: 'startsWith',
            caseSensitiveFilter: false,
            filterFunction: null,
            source: null,
            content: null,
            scrollHeight: 200
        },

        _create: function () {
            if (this.options.source) {
                for (var i = 0; i < this.options.source.length; i++) {
                    var choice = this.options.source[i];
                    if (choice.label)
                        this.element.append('<option value="' + choice.value + '">' + choice.label + '</option>');
                    else
                        this.element.append('<option value="' + choice + '">' + choice + '</option>');
                }
            }

            this.element.wrap('<div class="pui-dropdown ui-widget ui-state-default ui-corner-all ui-helper-clearfix" />')
                .wrap('<div class="ui-helper-hidden-accessible" />');
            this.container = this.element.closest('.pui-dropdown');
            this.focusElementContainer = $('<div class="ui-helper-hidden-accessible"><input type="text" /></div>').appendTo(this.container);
            this.focusElement = this.focusElementContainer.children('input');
            this.label = this.options.editable ? $('<input type="text" class="pui-dropdown-label pui-inputtext ui-corner-all"">')
                : $('<label class="pui-dropdown-label pui-inputtext ui-corner-all"/>');
            this.label.appendTo(this.container);
            this.menuIcon = $('<div class="pui-dropdown-trigger ui-state-default ui-corner-right"><span class="ui-icon ui-icon-triangle-1-s"></span></div>')
                .appendTo(this.container);
            this.panel = $('<div class="pui-dropdown-panel ui-widget-content ui-corner-all ui-helper-hidden pui-shadow" />').appendTo(document.body);
            this.itemsWrapper = $('<div class="pui-dropdown-items-wrapper" />').appendTo(this.panel);
            this.itemsContainer = $('<ul class="pui-dropdown-items pui-dropdown-list ui-widget-content ui-widget ui-corner-all ui-helper-reset"></ul>')
                .appendTo(this.itemsWrapper);
            this.disabled = this.element.prop('disabled');
            this.choices = this.element.children('option');
            this.optGroupsSize = this.itemsContainer.children('li.puiselectonemenu-item-group').length;

            if (this.options.filter) {
                this.filterContainer = $('<div class="pui-dropdown-filter-container" />').prependTo(this.panel);
                this.filterInput = $('<input type="text" autocomplete="off" class="pui-dropdown-filter pui-inputtext ui-widget ui-state-default ui-corner-all" />')
                    .appendTo(this.filterContainer);
                this.filterContainer.append('<span class="ui-icon ui-icon-search"></span>');
            }

            this._generateItems();

            var $this = this,
                selectedOption = this.choices.filter(':selected');

            //disable options
            this.choices.filter(':disabled').each(function () {
                $this.items.eq($(this).index()).addClass('ui-state-disabled');
            });

            //triggers
            this.triggers = this.options.editable ? this.menuIcon : this.container.children('.pui-dropdown-trigger, .pui-dropdown-label');

            //activate selected
            if (this.options.editable) {
                var customInputVal = this.label.val();

                //predefined input
                if (customInputVal === selectedOption.text()) {
                    this._highlightItem(this.items.eq(selectedOption.index()));
                }
                //custom input
                else {
                    this.items.eq(0).addClass('ui-state-highlight');
                    this.customInput = true;
                    this.customInputVal = customInputVal;
                }
            }
            else {
                this._highlightItem(this.items.eq(selectedOption.index()));
            }

            if (!this.disabled) {
                this._bindEvents();
                this._bindConstantEvents();
            }

            this._initDimensions();
        },

        _generateItems: function () {
            for (var i = 0; i < this.choices.length; i++) {
                var option = this.choices.eq(i),
                    optionLabel = option.text(),
                    content = this.options.content ? this.options.content.call(this, this.options.source[i]) : optionLabel;

                this.itemsContainer.append('<li data-label="' + optionLabel + '" class="pui-dropdown-item pui-dropdown-list-item ui-corner-all">' + content + '</li>');
            }

            this.items = this.itemsContainer.children('.pui-dropdown-item');
        },

        _bindEvents: function () {
            var $this = this;

            this.items.filter(':not(.ui-state-disabled)').each(function (i, item) {
                $this._bindItemEvents($(item));
            });

            this.triggers.on('mouseenter.puidropdown', function () {
                if (!$this.container.hasClass('ui-state-focus')) {
                    $this.container.addClass('ui-state-hover');
                    $this.menuIcon.addClass('ui-state-hover');
                }
            })
                .on('mouseleave.puidropdown', function () {
                    $this.container.removeClass('ui-state-hover');
                    $this.menuIcon.removeClass('ui-state-hover');
                })
                .on('click.puidropdown', function (e) {
                    if ($this.panel.is(":hidden")) {
                        $this._show();
                    }
                    else {
                        $this._hide();

                        $this._revert();
                    }

                    $this.container.removeClass('ui-state-hover');
                    $this.menuIcon.removeClass('ui-state-hover');
                    $this.focusElement.trigger('focus.puidropdown');
                    e.preventDefault();
                });

            this.focusElement.on('focus.puidropdown', function () {
                $this.container.addClass('ui-state-focus');
                $this.menuIcon.addClass('ui-state-focus');
            })
                .on('blur.puidropdown', function () {
                    $this.container.removeClass('ui-state-focus');
                    $this.menuIcon.removeClass('ui-state-focus');
                });

            if (this.options.editable) {
                this.label.on('change.pui-dropdown', function () {
                    $this._triggerChange(true);
                    $this.customInput = true;
                    $this.customInputVal = $(this).val();
                    $this.items.filter('.ui-state-highlight').removeClass('ui-state-highlight');
                    $this.items.eq(0).addClass('ui-state-highlight');
                });
            }

            this._bindKeyEvents();

            if (this.options.filter) {
                this._setupFilterMatcher();

                this.filterInput.puiinputtext();

                this.filterInput.on('keyup.pui-dropdown', function () {
                    $this._filter($(this).val());
                });
            }
        },

        _bindItemEvents: function (item) {
            var $this = this;

            item.on('mouseover.puidropdown', function () {
                var el = $(this);

                if (!el.hasClass('ui-state-highlight'))
                    $(this).addClass('ui-state-hover');
            })
                .on('mouseout.puidropdown', function () {
                    $(this).removeClass('ui-state-hover');
                })
                .on('click.puidropdown', function () {
                    $this._selectItem($(this));
                });
        },

        _bindConstantEvents: function () {
            var $this = this;

            $(document.body).bind('mousedown.pui-dropdown', function (e) {
                if ($this.panel.is(":hidden")) {
                    return;
                }

                var offset = $this.panel.offset();
                if (e.target === $this.label.get(0) ||
                    e.target === $this.menuIcon.get(0) ||
                    e.target === $this.menuIcon.children().get(0)) {
                    return;
                }

                if (e.pageX < offset.left ||
                    e.pageX > offset.left + $this.panel.width() ||
                    e.pageY < offset.top ||
                    e.pageY > offset.top + $this.panel.height()) {

                    $this._hide();
                    $this._revert();
                }
            });

            this.resizeNS = 'resize.' + this.id;
            this._unbindResize();
            this._bindResize();
        },

        _bindKeyEvents: function () {
            var $this = this;

            this.focusElement.on('keydown.puiselectonemenu', function (e) {
                var keyCode = $.ui.keyCode,
                    key = e.which;

                switch (key) {
                    case keyCode.UP:
                    case keyCode.LEFT:
                        var activeItem = $this._getActiveItem(),
                            prev = activeItem.prevAll(':not(.ui-state-disabled,.ui-selectonemenu-item-group):first');

                        if (prev.length == 1) {
                            if ($this.panel.is(':hidden')) {
                                $this._selectItem(prev);
                            }
                            else {
                                $this._highlightItem(prev);
                                PUI.scrollInView($this.itemsWrapper, prev);
                            }
                        }

                        e.preventDefault();
                        break;

                    case keyCode.DOWN:
                    case keyCode.RIGHT:
                        var activeItem = $this._getActiveItem(),
                            next = activeItem.nextAll(':not(.ui-state-disabled,.ui-selectonemenu-item-group):first');

                        if (next.length == 1) {
                            if ($this.panel.is(':hidden')) {
                                if (e.altKey) {
                                    $this._show();
                                } else {
                                    $this._selectItem(next);
                                }
                            }
                            else {
                                $this._highlightItem(next);
                                PUI.scrollInView($this.itemsWrapper, next);
                            }
                        }

                        e.preventDefault();
                        break;

                    case keyCode.ENTER:
                    case keyCode.NUMPAD_ENTER:
                        if ($this.panel.is(':hidden')) {
                            $this._show();
                        }
                        else {
                            $this._selectItem($this._getActiveItem());
                        }

                        e.preventDefault();
                        break;

                    case keyCode.TAB:
                        if ($this.panel.is(':visible')) {
                            $this._revert();
                            $this._hide();
                        }
                        break;

                    case keyCode.ESCAPE:
                        if ($this.panel.is(':visible')) {
                            $this._revert();
                            $this._hide();
                        }
                        break;

                    default:
                        var k = String.fromCharCode((96 <= key && key <= 105) ? key - 48 : key),
                            currentItem = $this.items.filter('.ui-state-highlight');

                        //Search items forward from current to end and on no result, search from start until current
                        var highlightItem = $this._search(k, currentItem.index() + 1, $this.options.length);
                        if (!highlightItem) {
                            highlightItem = $this._search(k, 0, currentItem.index());
                        }

                        if (highlightItem) {
                            if ($this.panel.is(':hidden')) {
                                $this._selectItem(highlightItem);
                            }
                            else {
                                $this._highlightItem(highlightItem);
                                PUI.scrollInView($this.itemsWrapper, highlightItem);
                            }
                        }

                        break;
                }
            });
        },

        _initDimensions: function () {
            var userStyle = this.element.attr('style');

            //do not adjust width of container if there is user width defined
            if (!userStyle || userStyle.indexOf('width') == -1) {
                this.container.width(this.element.outerWidth(true) + 5);
            }

            //width of label
            this.label.width(this.container.width() - this.menuIcon.width());

            //align panel and container
            var jqWidth = this.container.innerWidth();
            if (this.panel.outerWidth() < jqWidth) {
                this.panel.width(jqWidth);
            }

            this.element.parent().addClass('ui-helper-hidden').removeClass('ui-helper-hidden-accessible');

            if (this.options.scrollHeight && this.panel.outerHeight() > this.options.scrollHeight) {
                this.itemsWrapper.height(this.options.scrollHeight);
            }
        },

        _selectItem: function (item, silent) {
            var selectedOption = this.choices.eq(this._resolveItemIndex(item)),
                currentOption = this.choices.filter(':selected'),
                sameOption = selectedOption.val() == currentOption.val(),
                shouldChange = null;

            if (this.options.editable) {
                shouldChange = (!sameOption) || (selectedOption.text() != this.label.val());
            }
            else {
                shouldChange = !sameOption;
            }

            if (shouldChange) {
                this._highlightItem(item);
                this.element.val(selectedOption.val())

                this._triggerChange();

                if (this.options.editable) {
                    this.customInput = false;
                }
            }

            if (!silent) {
                this.focusElement.trigger('focus.puidropdown');
            }

            if (this.panel.is(':visible')) {
                this._hide();
            }
        },

        _highlightItem: function (item) {
            this.items.filter('.ui-state-highlight').removeClass('ui-state-highlight');
            item.addClass('ui-state-highlight');

            this._setLabel(item.data('label'));
        },

        _triggerChange: function (edited) {
            this.changed = false;

            if (this.options.change) {
                this._trigger('change');
            }

            if (!edited) {
                this.value = this.choices.filter(':selected').val();
            }
        },

        _resolveItemIndex: function (item) {
            if (this.optGroupsSize === 0)
                return item.index();
            else
                return item.index() - item.prevAll('li.pui-dropdown-item-group').length;
        },

        _setLabel: function (value) {
            if (this.options.editable) {
                this.label.val(value);
            }
            else {
                if (value === '&nbsp;')
                    this.label.html('&nbsp;');
                else
                    this.label.text(value);
            }
        },

        _bindResize: function () {
            var $this = this;

            $(window).bind(this.resizeNS, function (e) {
                if ($this.panel.is(':visible')) {
                    $this._alignPanel();
                }
            });
        },

        _unbindResize: function () {
            $(window).unbind(this.resizeNS);
        },

        _unbindEvents: function () {
            this.items.off();
            this.triggers.off();
            this.input.off();
            this.focusInput.off();
            this.label.off();
        },

        _alignPanel: function () {
            this.panel.css({left: '', top: ''}).position({
                my: 'left top', at: 'left bottom', of: this.container
            });
        },

        _show: function () {
            this._alignPanel();

            this.panel.css('z-index', ++PUI.zindex);

            if (this.options.effect != 'none')
                this.panel.show(this.options.effect, {}, this.options.effectSpeed);
            else
                this.panel.show();

            this.preShowValue = this.choices.filter(':selected');
        },

        _hide: function () {
            this.panel.hide();
        },

        _revert: function () {
            if (this.options.editable && this.customInput) {
                this._setLabel(this.customInputVal);
                this.items.filter('.ui-state-active').removeClass('ui-state-active');
                this.items.eq(0).addClass('ui-state-active');
            }
            else {
                this._highlightItem(this.items.eq(this.preShowValue.index()));
            }
        },

        _getActiveItem: function () {
            return this.items.filter('.ui-state-highlight');
        },

        _setupFilterMatcher: function () {
            this.filterMatchers = {
                'startsWith': this._startsWithFilter, 'contains': this._containsFilter, 'endsWith': this._endsWithFilter, 'custom': this.options.filterFunction
            };

            this.filterMatcher = this.filterMatchers[this.options.filterMatchMode];
        },

        _startsWithFilter: function (value, filter) {
            return value.indexOf(filter) === 0;
        },

        _containsFilter: function (value, filter) {
            return value.indexOf(filter) !== -1;
        },

        _endsWithFilter: function (value, filter) {
            return value.indexOf(filter, value.length - filter.length) !== -1;
        },

        _filter: function (value) {
            this.initialHeight = this.initialHeight || this.itemsWrapper.height();
            var filterValue = this.options.caseSensitiveFilter ? $.trim(value) : $.trim(value).toLowerCase();

            if (filterValue === '') {
                this.items.filter(':hidden').show();
            }
            else {
                for (var i = 0; i < this.choices.length; i++) {
                    var option = this.choices.eq(i),
                        itemLabel = this.options.caseSensitiveFilter ? option.text() : option.text().toLowerCase(),
                        item = this.items.eq(i);

                    if (this.filterMatcher(itemLabel, filterValue))
                        item.show();
                    else
                        item.hide();
                }
            }

            if (this.itemsContainer.height() < this.initialHeight) {
                this.itemsWrapper.css('height', 'auto');
            }
            else {
                this.itemsWrapper.height(this.initialHeight);
            }
        },

        _search: function (text, start, end) {
            for (var i = start; i < end; i++) {
                var option = this.choices.eq(i);

                if (option.text().indexOf(text) == 0) {
                    return this.items.eq(i);
                }
            }

            return null;
        },

        getSelectedValue: function () {
            return this.element.val();
        },

        getSelectedLabel: function () {
            return this.choices.filter(':selected').text();
        },

        selectValue: function (value) {
            var option = this.choices.filter('[value="' + value + '"]');

            this._selectItem(this.items.eq(option.index()), true);
        },

        addOption: function (label, value) {
            var item = $('<li data-label="' + label + '" class="pui-dropdown-item pui-dropdown-list-item ui-corner-all">' + label + '</li>'),
                choice = $('<option value="' + value + '">' + label + '</option>');

            choice.appendTo(this.element);
            this._bindItemEvents(item);
            item.appendTo(this.itemsContainer);
            this.items.add(item);
        }
    });

});
/**
 * PrimeFaces Fieldset Widget
 */
$(function () {

    $.widget("primeui.puifieldset", {

        options: {
            toggleable: false,
            toggleDuration: 'normal',
            collapsed: false
        },

        _create: function () {
            this.element.addClass('pui-fieldset ui-widget ui-widget-content ui-corner-all').
                children('legend').addClass('pui-fieldset-legend ui-corner-all ui-state-default');

            this.element.contents(':not(legend)').wrapAll('<div class="pui-fieldset-content" />');

            this.legend = this.element.children('legend.pui-fieldset-legend');
            this.content = this.element.children('div.pui-fieldset-content');

            this.legend.prependTo(this.element);

            if (this.options.toggleable) {
                this.element.addClass('pui-fieldset-toggleable');
                this.toggler = $('<span class="pui-fieldset-toggler ui-icon" />').prependTo(this.legend);

                this._bindEvents();

                if (this.options.collapsed) {
                    this.content.hide();
                    this.toggler.addClass('ui-icon-plusthick');
                } else {
                    this.toggler.addClass('ui-icon-minusthick');
                }
            }
        },

        _bindEvents: function () {
            var $this = this;

            this.legend.on('click.puifieldset', function (e) {
                $this.toggle(e);
            })
                .on('mouseover.puifieldset', function () {
                    $this.legend.addClass('ui-state-hover');
                })
                .on('mouseout.puifieldset', function () {
                    $this.legend.removeClass('ui-state-hover ui-state-active');
                })
                .on('mousedown.puifieldset', function () {
                    $this.legend.removeClass('ui-state-hover').addClass('ui-state-active');
                })
                .on('mouseup.puifieldset', function () {
                    $this.legend.removeClass('ui-state-active').addClass('ui-state-hover');
                })
        },

        toggle: function (e) {
            var $this = this;

            this._trigger('beforeToggle', e);

            if (this.options.collapsed) {
                this.toggler.removeClass('ui-icon-plusthick').addClass('ui-icon-minusthick');
            } else {
                this.toggler.removeClass('ui-icon-minusthick').addClass('ui-icon-plusthick');
            }

            this.content.slideToggle(this.options.toggleSpeed, 'easeInOutCirc', function () {
                $this._trigger('afterToggle', e);
                $this.options.collapsed = !$this.options.collapsed;
            });
        }

    });
});
/**
 * PrimeUI Lightbox Widget
 */
$(function () {

    $.widget("primeui.puigalleria", {

        options: {
            panelWidth: 600,
            panelHeight: 400,
            frameWidth: 60,
            frameHeight: 40,
            activeIndex: 0,
            showFilmstrip: true,
            autoPlay: true,
            transitionInterval: 4000,
            effect: 'fade',
            effectSpeed: 250,
            effectOptions: {},
            showCaption: true,
            customContent: false
        },

        _create: function () {
            this.element.addClass('pui-galleria ui-widget ui-widget-content ui-corner-all');
            this.panelWrapper = this.element.children('ul');
            this.panelWrapper.addClass('pui-galleria-panel-wrapper');
            this.panels = this.panelWrapper.children('li');
            this.panels.addClass('pui-galleria-panel ui-helper-hidden');

            this.element.width(this.options.panelWidth);
            this.panelWrapper.width(this.options.panelWidth).height(this.options.panelHeight);
            this.panels.width(this.options.panelWidth).height(this.options.panelHeight);

            if (this.options.showFilmstrip) {
                this._renderStrip();
                this._bindEvents();
            }

            if (this.options.customContent) {
                this.panels.children('img').remove();
                this.panels.children('div').addClass('pui-galleria-panel-content');
            }

            //show first
            var activePanel = this.panels.eq(this.options.activeIndex);
            activePanel.removeClass('ui-helper-hidden');
            if (this.options.showCaption) {
                this._showCaption(activePanel);
            }

            this.element.css('visibility', 'visible');

            if (this.options.autoPlay) {
                this.startSlideshow();
            }
        },

        _renderStrip: function () {
            var frameStyle = 'style="width:' + this.options.frameWidth + "px;height:" + this.options.frameHeight + 'px;"';

            this.stripWrapper = $('<div class="pui-galleria-filmstrip-wrapper"></div>')
                .width(this.element.width() - 50)
                .height(this.options.frameHeight)
                .appendTo(this.element);

            this.strip = $('<ul class="pui-galleria-filmstrip"></div>').appendTo(this.stripWrapper);

            for (var i = 0; i < this.panels.length; i++) {
                var image = this.panels.eq(i).children('img'),
                    frameClass = (i == this.options.activeIndex) ? 'pui-galleria-frame pui-galleria-frame-active' : 'pui-galleria-frame',
                    frameMarkup = '<li class="' + frameClass + '" ' + frameStyle + '>'
                        + '<div class="pui-galleria-frame-content" ' + frameStyle + '>'
                        + '<img src="' + image.attr('src') + '" class="pui-galleria-frame-image" ' + frameStyle + '/>'
                        + '</div></li>';

                this.strip.append(frameMarkup);
            }

            this.frames = this.strip.children('li.pui-galleria-frame');

            //navigators
            this.element.append('<div class="pui-galleria-nav-prev ui-icon ui-icon-circle-triangle-w" style="bottom:' + (this.options.frameHeight / 2) + 'px"></div>' +
                '<div class="pui-galleria-nav-next ui-icon ui-icon-circle-triangle-e" style="bottom:' + (this.options.frameHeight / 2) + 'px"></div>');

            //caption
            if (this.options.showCaption) {
                this.caption = $('<div class="pui-galleria-caption"></div>').css({
                    'bottom': this.stripWrapper.outerHeight(true),
                    'width': this.panelWrapper.width()
                }).appendTo(this.element);
            }
        },

        _bindEvents: function () {
            var $this = this;

            this.element.children('div.pui-galleria-nav-prev').on('click.puigalleria', function () {
                if ($this.slideshowActive) {
                    $this.stopSlideshow();
                }

                if (!$this.isAnimating()) {
                    $this.prev();
                }
            });

            this.element.children('div.pui-galleria-nav-next').on('click.puigalleria', function () {
                if ($this.slideshowActive) {
                    $this.stopSlideshow();
                }

                if (!$this.isAnimating()) {
                    $this.next();
                }
            });

            this.strip.children('li.pui-galleria-frame').on('click.puigalleria', function () {
                if ($this.slideshowActive) {
                    $this.stopSlideshow();
                }

                $this.select($(this).index(), false);
            });
        },

        startSlideshow: function () {
            var $this = this;

            this.interval = setInterval(function () {
                $this.next();
            }, this.options.transitionInterval);

            this.slideshowActive = true;
        },

        stopSlideshow: function () {
            clearInterval(this.interval);

            this.slideshowActive = false;
        },

        isSlideshowActive: function () {
            return this.slideshowActive;
        },

        select: function (index, reposition) {
            if (index !== this.options.activeIndex) {
                if (this.options.showCaption) {
                    this._hideCaption();
                }

                var oldPanel = this.panels.eq(this.options.activeIndex),
                    oldFrame = this.frames.eq(this.options.activeIndex),
                    newPanel = this.panels.eq(index),
                    newFrame = this.frames.eq(index);

                //content
                oldPanel.hide(this.options.effect, this.options.effectOptions, this.options.effectSpeed);
                newPanel.show(this.options.effect, this.options.effectOptions, this.options.effectSpeed);

                //frame
                oldFrame.removeClass('pui-galleria-frame-active').css('opacity', '');
                newFrame.animate({opacity: 1.0}, this.options.effectSpeed, null, function () {
                    $(this).addClass('pui-galleria-frame-active');
                });

                //caption
                if (this.options.showCaption) {
                    this._showCaption(newPanel);
                }

                //viewport
                if (reposition === undefined || reposition === true) {
                    var frameLeft = newFrame.position().left,
                        stepFactor = this.options.frameWidth + parseInt(newFrame.css('margin-right')),
                        stripLeft = this.strip.position().left,
                        frameViewportLeft = frameLeft + stripLeft,
                        frameViewportRight = frameViewportLeft + this.options.frameWidth;

                    if (frameViewportRight > this.stripWrapper.width()) {
                        this.strip.animate({left: '-=' + stepFactor}, this.options.effectSpeed, 'easeInOutCirc');
                    } else if (frameViewportLeft < 0) {
                        this.strip.animate({left: '+=' + stepFactor}, this.options.effectSpeed, 'easeInOutCirc');
                    }
                }

                this.options.activeIndex = index;
            }
        },

        _hideCaption: function () {
            this.caption.slideUp(this.options.effectSpeed);
        },

        _showCaption: function (panel) {
            var image = panel.children('img');
            this.caption.html('<h4>' + image.attr('title') + '</h4><p>' + image.attr('alt') + '</p>').slideDown(this.options.effectSpeed);
        },

        prev: function () {
            if (this.options.activeIndex != 0) {
                this.select(this.options.activeIndex - 1);
            }
        },

        next: function () {
            if (this.options.activeIndex !== (this.panels.length - 1)) {
                this.select(this.options.activeIndex + 1);
            }
            else {
                this.select(0, false);
                this.strip.animate({left: 0}, this.options.effectSpeed, 'easeInOutCirc');
            }
        },

        isAnimating: function () {
            return this.strip.is(':animated');
        }
    });
});
/**
 * PrimeFaces Growl Widget
 */
$(function () {

    $.widget("primeui.puigrowl", {

        options: {
            sticky: false,
            life: 3000
        },

        _create: function () {
            var container = this.element;

            container.addClass("pui-growl ui-widget").appendTo(document.body);
        },

        show: function (msgs) {
            var $this = this;

            //this.jq.css('z-index', ++PrimeFaces.zindex);

            this.clear();

            $.each(msgs, function (i, msg) {
                $this._renderMessage(msg);
            });
        },

        clear: function () {
            this.element.children('div.pui-growl-item-container').remove();
        },

        _renderMessage: function (msg) {
            var markup = '<div class="pui-growl-item-container ui-state-highlight ui-corner-all ui-helper-hidden" aria-live="polite">';
            markup += '<div class="pui-growl-item pui-shadow">';
            markup += '<div class="pui-growl-icon-close ui-icon ui-icon-closethick" style="display:none"></div>';
            markup += '<span class="pui-growl-image pui-growl-image-' + msg.severity + '" />';
            markup += '<div class="pui-growl-message">';
            markup += '<span class="pui-growl-title">' + msg.summary + '</span>';
            markup += '<p>' + (msg.detail || '') + '</p>';
            markup += '</div><div style="clear: both;"></div></div></div>';

            var message = $(markup);

            this._bindMessageEvents(message);
            message.appendTo(this.element).fadeIn();
        },

        _removeMessage: function (message) {
            message.fadeTo('normal', 0, function () {
                message.slideUp('normal', 'easeInOutCirc', function () {
                    message.remove();
                });
            });
        },

        _bindMessageEvents: function (message) {
            var $this = this,
                sticky = this.options.sticky;

            message.on('mouseover.puigrowl', function () {
                var msg = $(this);

                if (!msg.is(':animated')) {
                    msg.find('div.pui-growl-icon-close:first').show();
                }
            })
                .on('mouseout.puigrowl', function () {
                    $(this).find('div.pui-growl-icon-close:first').hide();
                });

            //remove message on click of close icon
            message.find('div.pui-growl-icon-close').on('click.puigrowl', function () {
                $this._removeMessage(message);

                if (!sticky) {
                    clearTimeout(message.data('timeout'));
                }
            });

            if (!sticky) {
                this._setRemovalTimeout(message);
            }
        },

        _setRemovalTimeout: function (message) {
            var $this = this;

            var timeout = setTimeout(function () {
                $this._removeMessage(message);
            }, this.options.life);

            message.data('timeout', timeout);
        }
    });
});
/**
 * PrimeUI inputtext widget
 */
$(function () {

    $.widget("primeui.puiinputtext", {

        _create: function () {
            var input = this.element,
                disabled = input.prop('disabled');

            //visuals
            input.addClass('pui-inputtext ui-widget ui-state-default ui-corner-all');

            if (disabled) {
                input.addClass('ui-state-disabled');
            }
            else {
                input.hover(function () {
                    input.toggleClass('ui-state-hover');
                }).focus(function () {
                        input.addClass('ui-state-focus');
                    }).blur(function () {
                        input.removeClass('ui-state-focus');
                    });
            }

            //aria
            input.attr('role', 'textbox').attr('aria-disabled', disabled)
                .attr('aria-readonly', input.prop('readonly'))
                .attr('aria-multiline', input.is('textarea'));
        },

        _destroy: function () {

        }

    });

});
/**
 * PrimeUI inputtextarea widget
 */
$(function () {

    $.widget("primeui.puiinputtextarea", {

        options: {
            autoResize: false, autoComplete: false, maxlength: null, counter: null, counterTemplate: '{0}', minQueryLength: 3, queryDelay: 700
        },

        _create: function () {
            var $this = this;

            this.element.puiinputtext();

            if (this.options.autoResize) {
                this.options.rowsDefault = this.element.attr('rows');
                this.options.colsDefault = this.element.attr('cols');

                this.element.addClass('pui-inputtextarea-resizable');

                this.element.keyup(function () {
                    $this._resize();
                }).focus(function () {
                        $this._resize();
                    }).blur(function () {
                        $this._resize();
                    });
            }

            if (this.options.maxlength) {
                this.element.keyup(function (e) {
                    var value = $this.element.val(),
                        length = value.length;

                    if (length > $this.options.maxlength) {
                        $this.element.val(value.substr(0, $this.options.maxlength));
                    }

                    if ($this.options.counter) {
                        $this._updateCounter();
                    }
                });
            }

            if (this.options.counter) {
                this._updateCounter();
            }

            if (this.options.autoComplete) {
                this._initAutoComplete();
            }
        },

        _updateCounter: function () {
            var value = this.element.val(),
                length = value.length;

            if (this.options.counter) {
                var remaining = this.options.maxlength - length,
                    remainingText = this.options.counterTemplate.replace('{0}', remaining);

                this.options.counter.text(remainingText);
            }
        },

        _resize: function () {
            var linesCount = 0,
                lines = this.element.val().split('\n');

            for (var i = lines.length - 1; i >= 0; --i) {
                linesCount += Math.floor((lines[i].length / this.options.colsDefault) + 1);
            }

            var newRows = (linesCount >= this.options.rowsDefault) ? (linesCount + 1) : this.options.rowsDefault;

            this.element.attr('rows', newRows);
        },


        _initAutoComplete: function () {
            var panelMarkup = '<div id="' + this.id + '_panel" class="pui-autocomplete-panel ui-widget-content ui-corner-all ui-helper-hidden ui-shadow"></div>',
                $this = this;

            this.panel = $(panelMarkup).appendTo(document.body);

            this.element.keyup(function (e) {
                var keyCode = $.ui.keyCode;

                switch (e.which) {

                    case keyCode.UP:
                    case keyCode.LEFT:
                    case keyCode.DOWN:
                    case keyCode.RIGHT:
                    case keyCode.ENTER:
                    case keyCode.NUMPAD_ENTER:
                    case keyCode.TAB:
                    case keyCode.SPACE:
                    case keyCode.CONTROL:
                    case keyCode.ALT:
                    case keyCode.ESCAPE:
                    case 224:   //mac command
                        //do not search
                        break;

                    default:
                        var query = $this._extractQuery();
                        if (query && query.length >= $this.options.minQueryLength) {

                            //Cancel the search request if user types within the timeout
                            if ($this.timeout) {
                                $this._clearTimeout($this.timeout);
                            }

                            $this.timeout = setTimeout(function () {
                                $this.search(query);
                            }, $this.options.queryDelay);

                        }
                        break;
                }

            }).keydown(function (e) {
                    var overlayVisible = $this.panel.is(':visible'),
                        keyCode = $.ui.keyCode;

                    switch (e.which) {
                        case keyCode.UP:
                        case keyCode.LEFT:
                            if (overlayVisible) {
                                var highlightedItem = $this.items.filter('.ui-state-highlight'),
                                    prev = highlightedItem.length == 0 ? $this.items.eq(0) : highlightedItem.prev();

                                if (prev.length == 1) {
                                    highlightedItem.removeClass('ui-state-highlight');
                                    prev.addClass('ui-state-highlight');

                                    if ($this.options.scrollHeight) {
                                        PUI.scrollInView($this.panel, prev);
                                    }
                                }

                                e.preventDefault();
                            }
                            else {
                                $this._clearTimeout();
                            }
                            break;

                        case keyCode.DOWN:
                        case keyCode.RIGHT:
                            if (overlayVisible) {
                                var highlightedItem = $this.items.filter('.ui-state-highlight'),
                                    next = highlightedItem.length == 0 ? _self.items.eq(0) : highlightedItem.next();

                                if (next.length == 1) {
                                    highlightedItem.removeClass('ui-state-highlight');
                                    next.addClass('ui-state-highlight');

                                    if ($this.options.scrollHeight) {
                                        PUI.scrollInView($this.panel, next);
                                    }
                                }

                                e.preventDefault();
                            }
                            else {
                                $this._clearTimeout();
                            }
                            break;

                        case keyCode.ENTER:
                        case keyCode.NUMPAD_ENTER:
                            if (overlayVisible) {
                                $this.items.filter('.ui-state-highlight').trigger('click');

                                e.preventDefault();
                            }
                            else {
                                $this._clearTimeout();
                            }
                            break;

                        case keyCode.SPACE:
                        case keyCode.CONTROL:
                        case keyCode.ALT:
                        case keyCode.BACKSPACE:
                        case keyCode.ESCAPE:
                        case 224:   //mac command
                            $this._clearTimeout();

                            if (overlayVisible) {
                                $this._hide();
                            }
                            break;

                        case keyCode.TAB:
                            $this._clearTimeout();

                            if (overlayVisible) {
                                $this.items.filter('.ui-state-highlight').trigger('click');
                                $this._hide();
                            }
                            break;
                    }
                });

            //hide panel when outside is clicked
            $(document.body).bind('mousedown.puiinputtextarea', function (e) {
                if ($this.panel.is(":hidden")) {
                    return;
                }
                var offset = $this.panel.offset();
                if (e.target === $this.element.get(0)) {
                    return;
                }

                if (e.pageX < offset.left ||
                    e.pageX > offset.left + $this.panel.width() ||
                    e.pageY < offset.top ||
                    e.pageY > offset.top + $this.panel.height()) {
                    $this._hide();
                }
            });

            //Hide overlay on resize
            var resizeNS = 'resize.' + this.id;
            $(window).unbind(resizeNS).bind(resizeNS, function () {
                if ($this.panel.is(':visible')) {
                    $this._hide();
                }
            });
        },

        _bindDynamicEvents: function () {
            var $this = this;

            //visuals and click handler for items
            this.items.bind('mouseover', function () {
                var item = $(this);

                if (!item.hasClass('ui-state-highlight')) {
                    $this.items.filter('.ui-state-highlight').removeClass('ui-state-highlight');
                    item.addClass('ui-state-highlight');
                }
            })
                .bind('click', function (event) {
                    var item = $(this),
                        itemValue = item.attr('data-item-value'),
                        insertValue = itemValue.substring($this.query.length);

                    $this.element.focus();

                    $this.element.insertText(insertValue, $this.element.getSelection().start, true);

                    $this._hide();

                    $this._trigger("itemselect", event, item);
                });
        },

        _clearTimeout: function () {
            if (this.timeout) {
                clearTimeout(this.timeout);
            }

            this.timeout = null;
        },

        _extractQuery: function () {
            var end = this.element.getSelection().end,
                result = /\S+$/.exec(this.element.get(0).value.slice(0, end)),
                lastWord = result ? result[0] : null;

            return lastWord;
        },

        search: function (q) {
            this.query = q;

            var request = {
                query: q
            };

            if (this.options.completeSource) {
                this.options.completeSource.call(this, request, this._handleResponse);
            }
        },

        _handleResponse: function (data) {
            this.panel.html('');

            var listContainer = $('<ul class="pui-autocomplete-items pui-autocomplete-list ui-widget-content ui-widget ui-corner-all ui-helper-reset"></ul>');

            for (var i = 0; i < data.length; i++) {
                var item = $('<li class="pui-autocomplete-item pui-autocomplete-list-item ui-corner-all"></li>');
                item.attr('data-item-value', data[i].value);
                item.text(data[i].label);

                listContainer.append(item);
            }

            this.panel.append(listContainer).show();
            this.items = this.panel.find('.pui-autocomplete-item');

            this._bindDynamicEvents();

            if (this.items.length > 0) {
                //highlight first item
                this.items.eq(0).addClass('ui-state-highlight');

                //adjust height
                if (this.options.scrollHeight && this.panel.height() > this.options.scrollHeight) {
                    this.panel.height(this.options.scrollHeight);
                }

                if (this.panel.is(':hidden')) {
                    this._show();
                }
                else {
                    this._alignPanel(); //with new items
                }

            }
            else {
                this.panel.hide();
            }
        },

        _alignPanel: function () {
            var pos = this.element.getCaretPosition(),
                offset = this.element.offset();

            this.panel.css({
                'left': offset.left + pos.left,
                'top': offset.top + pos.top,
                'width': this.element.innerWidth()
            });
        },

        _show: function () {
            this._alignPanel();

            this.panel.show();
        },

        _hide: function () {
            this.panel.hide();
        }
    });

});
/**
 * PrimeUI Lightbox Widget
 */
$(function () {

    $.widget("primeui.puilightbox", {

        options: {
            iframeWidth: 640,
            iframeHeight: 480,
            iframe: false
        },

        _create: function () {
            this.options.mode = this.options.iframe ? 'iframe' : (this.element.children('div').length == 1) ? 'inline' : 'image';

            var dom = '<div class="pui-lightbox ui-widget ui-helper-hidden ui-corner-all pui-shadow">';
            dom += '<div class="pui-lightbox-content-wrapper">';
            dom += '<a class="ui-state-default pui-lightbox-nav-left ui-corner-right ui-helper-hidden"><span class="ui-icon ui-icon-carat-1-w">go</span></a>';
            dom += '<div class="pui-lightbox-content ui-corner-all"></div>';
            dom += '<a class="ui-state-default pui-lightbox-nav-right ui-corner-left ui-helper-hidden"><span class="ui-icon ui-icon-carat-1-e">go</span></a>';
            dom += '</div>';
            dom += '<div class="pui-lightbox-caption ui-widget-header"><span class="pui-lightbox-caption-text"></span>';
            dom += '<a class="pui-lightbox-close ui-corner-all" href="#"><span class="ui-icon ui-icon-closethick"></span></a><div style="clear:both" /></div>';
            dom += '</div>';

            this.panel = $(dom).appendTo(document.body);
            this.contentWrapper = this.panel.children('.pui-lightbox-content-wrapper');
            this.content = this.contentWrapper.children('.pui-lightbox-content');
            this.caption = this.panel.children('.pui-lightbox-caption');
            this.captionText = this.caption.children('.pui-lightbox-caption-text');
            this.closeIcon = this.caption.children('.pui-lightbox-close');

            if (this.options.mode === 'image') {
                this._setupImaging();
            }
            else if (this.options.mode === 'inline') {
                this._setupInline();
            }
            else if (this.options.mode === 'iframe') {
                this._setupIframe();
            }

            this._bindCommonEvents();

            this.links.data('puilightbox-trigger', true).find('*').data('puilightbox-trigger', true);
            this.closeIcon.data('puilightbox-trigger', true).find('*').data('puilightbox-trigger', true);
        },

        _bindCommonEvents: function () {
            var $this = this;

            this.closeIcon.hover(function () {
                $(this).toggleClass('ui-state-hover');
            }).click(function (e) {
                    $this.hide();
                    e.preventDefault();
                });

            //hide when outside is clicked
            $(document.body).bind('click.pui-lightbox', function (e) {
                if ($this.isHidden()) {
                    return;
                }

                //do nothing if target is the link
                var target = $(e.target);
                if (target.data('puilightbox-trigger')) {
                    return;
                }

                //hide if mouse is outside of lightbox
                var offset = $this.panel.offset();
                if (e.pageX < offset.left ||
                    e.pageX > offset.left + $this.panel.width() ||
                    e.pageY < offset.top ||
                    e.pageY > offset.top + $this.panel.height()) {

                    $this.hide();
                }
            });

            //sync window resize
            $(window).resize(function () {
                if (!$this.isHidden()) {
                    $(document.body).children('.ui-widget-overlay').css({
                        'width': $(document).width(), 'height': $(document).height()
                    });
                }
            });
        },

        _setupImaging: function () {
            var $this = this;

            this.links = this.element.children('a');
            this.content.append('<img class="ui-helper-hidden"></img>');
            this.imageDisplay = this.content.children('img');
            this.navigators = this.contentWrapper.children('a');

            this.imageDisplay.load(function () {
                var image = $(this);

                $this._scaleImage(image);

                //coordinates to center overlay
                var leftOffset = ($this.panel.width() - image.width()) / 2,
                    topOffset = ($this.panel.height() - image.height()) / 2;

                //resize content for new image
                $this.content.removeClass('pui-lightbox-loading').animate({
                        width: image.width(), height: image.height()
                    },
                    500,
                    function () {
                        //show image
                        image.fadeIn();
                        $this._showNavigators();
                        $this.caption.slideDown();
                    });

                $this.panel.animate({
                    left: '+=' + leftOffset, top: '+=' + topOffset
                }, 500);
            });

            this.navigators.hover(function () {
                $(this).toggleClass('ui-state-hover');
            })
                .click(function (e) {
                    var nav = $(this);

                    $this._hideNavigators();

                    if (nav.hasClass('pui-lightbox-nav-left')) {
                        var index = $this.current == 0 ? $this.links.length - 1 : $this.current - 1;

                        $this.links.eq(index).trigger('click');
                    }
                    else {
                        var index = $this.current == $this.links.length - 1 ? 0 : $this.current + 1;

                        $this.links.eq(index).trigger('click');
                    }

                    e.preventDefault();
                });

            this.links.click(function (e) {
                var link = $(this);

                if ($this.isHidden()) {
                    $this.content.addClass('pui-lightbox-loading').width(32).height(32);
                    $this.show();
                }
                else {
                    $this.imageDisplay.fadeOut(function () {
                        //clear for onload scaling
                        $(this).css({
                            'width': 'auto', 'height': 'auto'
                        });

                        $this.content.addClass('pui-lightbox-loading');
                    });

                    $this.caption.slideUp();
                }

                setTimeout(function () {
                    $this.imageDisplay.attr('src', link.attr('href'));
                    $this.current = link.index();

                    var title = link.attr('title');
                    if (title) {
                        $this.captionText.html(title);
                    }
                }, 1000);


                e.preventDefault();
            });
        },

        _scaleImage: function (image) {
            var win = $(window),
                winWidth = win.width(),
                winHeight = win.height(),
                imageWidth = image.width(),
                imageHeight = image.height(),
                ratio = imageHeight / imageWidth;

            if (imageWidth >= winWidth && ratio <= 1) {
                imageWidth = winWidth * 0.75;
                imageHeight = imageWidth * ratio;
            }
            else if (imageHeight >= winHeight) {
                imageHeight = winHeight * 0.75;
                imageWidth = imageHeight / ratio;
            }

            image.css({
                'width': imageWidth + 'px', 'height': imageHeight + 'px'
            })
        },

        _setupInline: function () {
            this.links = this.element.children('a');
            this.inline = this.element.children('div').addClass('pui-lightbox-inline');
            this.inline.appendTo(this.content).show();
            var $this = this;

            this.links.click(function (e) {
                $this.show();

                var title = $(this).attr('title');
                if (title) {
                    $this.captionText.html(title);
                    $this.caption.slideDown();
                }

                e.preventDefault();
            });
        },

        _setupIframe: function () {
            var $this = this;
            this.links = this.element;
            this.iframe = $('<iframe frameborder="0" style="width:' + this.options.iframeWidth + 'px;height:'
                + this.options.iframeHeight + 'px;border:0 none; display: block;"></iframe>').appendTo(this.content);

            if (this.options.iframeTitle) {
                this.iframe.attr('title', this.options.iframeTitle);
            }

            this.element.click(function (e) {
                if (!$this.iframeLoaded) {
                    $this.content.addClass('pui-lightbox-loading').css({
                        width: $this.options.iframeWidth, height: $this.options.iframeHeight
                    });

                    $this.show();

                    $this.iframe.on('load', function () {
                        $this.iframeLoaded = true;
                        $this.content.removeClass('pui-lightbox-loading');
                    })
                        .attr('src', $this.element.attr('href'));
                }
                else {
                    $this.show();
                }

                var title = $this.element.attr('title');
                if (title) {
                    $this.caption.html(title);
                    $this.caption.slideDown();
                }

                e.preventDefault();
            });
        },

        show: function () {
            this.center();

            this.panel.css('z-index', ++PUI.zindex).show();

            if (!this.modality) {
                this._enableModality();
            }

            this._trigger('show');
        },

        hide: function () {
            this.panel.fadeOut();
            this._disableModality();
            this.caption.hide();

            if (this.options.mode === 'image') {
                this.imageDisplay.hide().attr('src', '').removeAttr('style');
                this._hideNavigators();
            }

            this._trigger('hide');
        },

        center: function () {
            var win = $(window),
                left = (win.width() / 2 ) - (this.panel.width() / 2),
                top = (win.height() / 2 ) - (this.panel.height() / 2);

            this.panel.css({
                'left': left,
                'top': top
            });
        },

        _enableModality: function () {
            this.modality = $('<div class="ui-widget-overlay"></div>')
                .css({
                    'width': $(document).width(), 'height': $(document).height(), 'z-index': this.panel.css('z-index') - 1
                })
                .appendTo(document.body);
        },

        _disableModality: function () {
            this.modality.remove();
            this.modality = null;
        },

        _showNavigators: function () {
            this.navigators.zIndex(this.imageDisplay.zIndex() + 1).show();
        },

        _hideNavigators: function () {
            this.navigators.hide();
        },

        isHidden: function () {
            return this.panel.is(':hidden');
        },

        showURL: function (opt) {
            if (opt.width)
                this.iframe.attr('width', opt.width);
            if (opt.height)
                this.iframe.attr('height', opt.height);

            this.iframe.attr('src', opt.src);

            this.show();
        }
    });
});
/**
 * PrimeUI listvox widget
 */
$(function () {

    $.widget("primeui.puilistbox", {

        options: {
            scrollHeight: 200
        },

        _create: function () {
            this.element.wrap('<div class="pui-listbox pui-inputtext ui-widget ui-widget-content ui-corner-all"><div class="ui-helper-hidden-accessible"></div></div>');
            this.container = this.element.parent().parent();
            this.listContainer = $('<ul class="pui-listbox-list"></ul>').appendTo(this.container);
            this.options.multiple = this.element.prop("multiple");

            if (this.options.data) {
                for (var i = 0; i < this.options.data.length; i++) {
                    var choice = this.options.data[i];
                    if (choice.label)
                        this.element.append('<option value="' + choice.value + '">' + choice.label + '</option>');
                    else
                        this.element.append('<option value="' + choice + '">' + choice + '</option>');
                }
            }

            this.choices = this.element.children('option');
            for (var i = 0; i < this.choices.length; i++) {
                var choice = this.choices.eq(i),
                    content = this.options.content ? this.options.content.call(this, this.options.data[i]) : choice.text();
                this.listContainer.append('<li class="pui-listbox-item ui-corner-all">' + content + '</li>');
            }

            this.items = this.listContainer.find('.pui-listbox-item:not(.ui-state-disabled)');

            if (this.container.height() > this.options.scrollHeight) {
                this.container.height(this.options.scrollHeight);
            }

            if (this.options.width) {
                this.container.width(this.options.width);
            }

            this._bindEvents();
        },

        _bindEvents: function () {
            var $this = this;

            //items
            this.items.on('mouseover.puilistbox', function () {
                var item = $(this);
                if (!item.hasClass('ui-state-highlight')) {
                    item.addClass('ui-state-hover');
                }
            })
                .on('mouseout.puilistbox', function () {
                    $(this).removeClass('ui-state-hover');
                })
                .on('dblclick.puilistbox', function (e) {
                    $this.element.trigger('dblclick');

                    PUI.clearSelection();
                    e.preventDefault();
                })
                .on('click.puilistbox', function (e) {
                    if ($this.options.multiple)
                        $this._clickMultiple(e, $(this));
                    else
                        $this._clickSingle(e, $(this));
                });

            //input
            this.element.on('focus.puilistbox',function () {
                $this.container.addClass('ui-state-focus');
            }).on('blur.puilistbox', function () {
                    $this.container.removeClass('ui-state-focus');
                });
        },

        _clickSingle: function (event, item) {
            var selectedItem = this.items.filter('.ui-state-highlight');

            if (item.index() !== selectedItem.index()) {
                if (selectedItem.length) {
                    this.unselectItem(selectedItem);
                }

                this.selectItem(item);
                this.element.trigger('change');
            }

            this.element.trigger('click');

            PUI.clearSelection();

            event.preventDefault();
        },

        _clickMultiple: function (event, item) {
            var selectedItems = this.items.filter('.ui-state-highlight'),
                metaKey = (event.metaKey || event.ctrlKey),
                unchanged = (!metaKey && selectedItems.length === 1 && selectedItems.index() === item.index());

            if (!event.shiftKey) {
                if (!metaKey) {
                    this.unselectAll();
                }

                if (metaKey && item.hasClass('ui-state-highlight')) {
                    this.unselectItem(item);
                }
                else {
                    this.selectItem(item);
                    this.cursorItem = item;
                }
            }
            else {
                //range selection
                if (this.cursorItem) {
                    this.unselectAll();

                    var currentItemIndex = item.index(),
                        cursorItemIndex = this.cursorItem.index(),
                        startIndex = (currentItemIndex > cursorItemIndex) ? cursorItemIndex : currentItemIndex,
                        endIndex = (currentItemIndex > cursorItemIndex) ? (currentItemIndex + 1) : (cursorItemIndex + 1);

                    for (var i = startIndex; i < endIndex; i++) {
                        this.selectItem(this.items.eq(i));
                    }
                }
                else {
                    this.selectItem(item);
                    this.cursorItem = item;
                }
            }

            if (!unchanged) {
                this.element.trigger('change');
            }

            this.element.trigger('click');
            PUI.clearSelection();
            event.preventDefault();
        },

        unselectAll: function () {
            this.items.removeClass('ui-state-highlight ui-state-hover');
            this.choices.filter(':selected').prop('selected', false);
        },

        selectItem: function (value) {
            var item = null;
            if ($.type(value) === 'number')
                item = this.items.eq(value);
            else
                item = value;

            item.addClass('ui-state-highlight').removeClass('ui-state-hover');
            this.choices.eq(item.index()).prop('selected', true);
            this._trigger('itemSelect', null, this.choices.eq(item.index()));
        },

        unselectItem: function (value) {
            var item = null;
            if ($.type(value) === 'number')
                item = this.items.eq(value);
            else
                item = value;

            item.removeClass('ui-state-highlight');
            this.choices.eq(item.index()).prop('selected', false);
            this._trigger('itemUnselect', null, this.choices.eq(item.index()));
        }
    });

});
/**
 * PrimeUI BaseMenu widget
 */
$(function () {

    $.widget("primeui.puibasemenu", {

        options: {
            popup: false,
            trigger: null,
            my: 'left top',
            at: 'left bottom',
            triggerEvent: 'click'
        },

        _create: function () {
            if (this.options.popup) {
                this._initPopup();
            }
        },

        _initPopup: function () {
            var $this = this;

            this.element.closest('.pui-menu').addClass('pui-menu-dynamic pui-shadow').appendTo(document.body);

            this.positionConfig = {
                my: this.options.my, at: this.options.at, of: this.options.trigger
            }

            this.options.trigger.on(this.options.triggerEvent + '.pui-menu', function (e) {
                var trigger = $(this);

                if ($this.element.is(':visible')) {
                    $this.hide();
                }
                else {
                    $this.show();
                }

                e.preventDefault();
            });

            //hide overlay on document click
            $(document.body).on('click.pui-menu', function (e) {
                var popup = $this.element.closest('.pui-menu');
                if (popup.is(":hidden")) {
                    return;
                }

                //do nothing if mousedown is on trigger
                var target = $(e.target);
                if (target.is($this.options.trigger.get(0)) || $this.options.trigger.has(target).length > 0) {
                    return;
                }

                //hide if mouse is outside of overlay except trigger
                var offset = popup.offset();
                if (e.pageX < offset.left ||
                    e.pageX > offset.left + popup.width() ||
                    e.pageY < offset.top ||
                    e.pageY > offset.top + popup.height()) {

                    $this.hide(e);
                }
            });

            //Hide overlay on resize
            $(window).on('resize.pui-menu', function () {
                if ($this.element.closest('.pui-menu').is(':visible')) {
                    $this.align();
                }
            });
        },

        show: function () {
            this.align();
            this.element.closest('.pui-menu').css('z-index', ++PUI.zindex).show();
        },

        hide: function () {
            this.element.closest('.pui-menu').fadeOut('fast');
        },

        align: function () {
            this.element.closest('.pui-menu').css({left: '', top: ''}).position(this.positionConfig);
        }
    });
});

/**
 * PrimeUI Menu widget
 */
$(function () {

    $.widget("primeui.puimenu", $.primeui.puibasemenu, {

        options: {

        },

        _create: function () {
            this.element.addClass('pui-menu-list ui-helper-reset').
                wrap('<div class="pui-menu ui-widget ui-widget-content ui-corner-all ui-helper-clearfix" />');

            this.element.children('li').each(function () {
                var listItem = $(this);

                if (listItem.children('h3').length > 0) {
                    listItem.addClass('ui-widget-header ui-corner-all');
                }
                else {
                    listItem.addClass('pui-menuitem ui-widget ui-corner-all');
                    var menuitemLink = listItem.children('a'),
                        icon = menuitemLink.data('icon');

                    menuitemLink.addClass('pui-menuitem-link ui-corner-all').contents().wrap('<span class="ui-menuitem-text" />');

                    if (icon) {
                        menuitemLink.prepend('<span class="pui-menuitem-icon ui-icon ' + icon + '"></span>');
                    }
                }
            });

            this.menuitemLinks = this.element.find('.pui-menuitem-link:not(.ui-state-disabled)');

            this._bindEvents();

            this._super();
        },

        _bindEvents: function () {
            var $this = this;

            this.menuitemLinks.on('mouseenter.pui-menu', function (e) {
                $(this).addClass('ui-state-hover');
            })
                .on('mouseleave.pui-menu', function (e) {
                    $(this).removeClass('ui-state-hover');
                });

            if (this.options.popup) {
                this.menuitemLinks.on('click.pui-menu', function () {
                    $this.hide();
                });
            }
        }
    });
});

/**
 * PrimeUI BreadCrumb Widget
 */
$(function () {

    $.widget("primeui.puibreadcrumb", {

        _create: function () {
            this.element.wrap('<div class="pui-breadcrumb ui-module ui-widget ui-widget-header ui-helper-clearfix ui-corner-all" role="menu">');

            this.element.children('li').each(function (index) {
                var listItem = $(this);

                listItem.attr('role', 'menuitem');
                var menuitemLink = listItem.children('a');
                menuitemLink.addClass('pui-menuitem-link ui-corner-all').contents().wrap('<span class="ui-menuitem-text" />');

                if (index > 0)
                    listItem.before('<li class="pui-breadcrumb-chevron ui-icon ui-icon-triangle-1-e"></li>');
                else
                    menuitemLink.addClass('ui-icon ui-icon-home');
            });
        }
    });
});


/*
 * PrimeUI TieredMenu Widget
 */
$(function () {

    $.widget("primeui.puitieredmenu", $.primeui.puibasemenu, {

        options: {
            autoDisplay: true
        },

        _create: function () {
            this._render();

            this.links = this.element.find('.pui-menuitem-link:not(.ui-state-disabled)');

            this._bindEvents();

            this._super();
        },

        _render: function () {
            this.element.addClass('pui-menu-list ui-helper-reset').
                wrap('<div class="pui-tieredmenu pui-menu ui-widget ui-widget-content ui-corner-all ui-helper-clearfix" />');

            this.element.parent().uniqueId();
            this.options.id = this.element.parent().attr('id');

            this.element.find('li').each(function () {
                var listItem = $(this),
                    menuitemLink = listItem.children('a'),
                    icon = menuitemLink.data('icon');

                menuitemLink.addClass('pui-menuitem-link ui-corner-all').contents().wrap('<span class="ui-menuitem-text" />');

                if (icon) {
                    menuitemLink.prepend('<span class="pui-menuitem-icon ui-icon ' + icon + '"></span>');
                }

                listItem.addClass('pui-menuitem ui-widget ui-corner-all');
                if (listItem.children('ul').length > 0) {
                    listItem.addClass('pui-menu-parent');
                    listItem.children('ul').addClass('ui-widget-content pui-menu-list ui-corner-all ui-helper-clearfix pui-menu-child pui-shadow');
                    menuitemLink.prepend('<span class="ui-icon ui-icon-triangle-1-e"></span>');
                }


            });
        },

        _bindEvents: function () {
            this._bindItemEvents();

            this._bindDocumentHandler();
        },

        _bindItemEvents: function () {
            var $this = this;

            this.links.on('mouseenter.pui-menu', function () {
                var link = $(this),
                    menuitem = link.parent(),
                    autoDisplay = $this.options.autoDisplay;

                var activeSibling = menuitem.siblings('.pui-menuitem-active');
                if (activeSibling.length === 1) {
                    $this._deactivate(activeSibling);
                }

                if (autoDisplay || $this.active) {
                    if (menuitem.hasClass('pui-menuitem-active')) {
                        $this._reactivate(menuitem);
                    }
                    else {
                        $this._activate(menuitem);
                    }
                }
                else {
                    $this._highlight(menuitem);
                }
            });

            if (this.options.autoDisplay === false) {
                this.rootLinks = this.element.find('> .pui-menuitem > .pui-menuitem-link');
                this.rootLinks.data('primeui-tieredmenu-rootlink', this.options.id).find('*').data('primeui-tieredmenu-rootlink', this.options.id)

                this.rootLinks.on('click.pui-menu', function (e) {
                    var link = $(this),
                        menuitem = link.parent(),
                        submenu = menuitem.children('ul.pui-menu-child');

                    if (submenu.length === 1) {
                        if (submenu.is(':visible')) {
                            $this.active = false;
                            $this._deactivate(menuitem);
                        }
                        else {
                            $this.active = true;
                            $this._highlight(menuitem);
                            $this._showSubmenu(menuitem, submenu);
                        }
                    }
                });
            }

            this.element.parent().find('ul.pui-menu-list').on('mouseleave.pui-menu', function (e) {
                if ($this.activeitem) {
                    $this._deactivate($this.activeitem);
                }

                e.stopPropagation();
            });
        },

        _bindDocumentHandler: function () {
            var $this = this;

            $(document.body).on('click.pui-menu', function (e) {
                var target = $(e.target);
                if (target.data('primeui-tieredmenu-rootlink') === $this.options.id) {
                    return;
                }

                $this.active = false;

                $this.element.find('li.pui-menuitem-active').each(function () {
                    $this._deactivate($(this), true);
                });
            });
        },

        _deactivate: function (menuitem, animate) {
            this.activeitem = null;
            menuitem.children('a.pui-menuitem-link').removeClass('ui-state-hover');
            menuitem.removeClass('pui-menuitem-active');

            if (animate)
                menuitem.children('ul.pui-menu-child:visible').fadeOut('fast');
            else
                menuitem.children('ul.pui-menu-child:visible').hide();
        },

        _activate: function (menuitem) {
            this._highlight(menuitem);

            var submenu = menuitem.children('ul.pui-menu-child');
            if (submenu.length === 1) {
                this._showSubmenu(menuitem, submenu);
            }
        },

        _reactivate: function (menuitem) {
            this.activeitem = menuitem;
            var submenu = menuitem.children('ul.pui-menu-child'),
                activeChilditem = submenu.children('li.pui-menuitem-active:first'),
                _self = this;

            if (activeChilditem.length === 1) {
                _self._deactivate(activeChilditem);
            }
        },

        _highlight: function (menuitem) {
            this.activeitem = menuitem;
            menuitem.children('a.pui-menuitem-link').addClass('ui-state-hover');
            menuitem.addClass('pui-menuitem-active');
        },

        _showSubmenu: function (menuitem, submenu) {
            submenu.css({
                'left': menuitem.outerWidth(), 'top': 0, 'z-index': ++PUI.zindex
            });

            submenu.show();
        }

    });

});

/**
 * PrimeUI Menubar Widget
 */

$(function () {

    $.widget("primeui.puimenubar", $.primeui.puitieredmenu, {

        options: {
            autoDisplay: true
        },

        _create: function () {
            this._super();
            this.element.parent().removeClass('pui-tieredmenu').
                addClass('pui-menubar');
        },

        _showSubmenu: function (menuitem, submenu) {
            var win = $(window),
                submenuOffsetTop = null,
                submenuCSS = {
                    'z-index': ++PUI.zindex
                };

            if (menuitem.parent().hasClass('pui-menu-child')) {
                submenuCSS.left = menuitem.outerWidth();
                submenuCSS.top = 0;
                submenuOffsetTop = menuitem.offset().top - win.scrollTop();
            }
            else {
                submenuCSS.left = 0;
                submenuCSS.top = menuitem.outerHeight();
                menuitem.offset().top - win.scrollTop();
                submenuOffsetTop = menuitem.offset().top + submenuCSS.top - win.scrollTop();
            }

            //adjust height within viewport
            submenu.css('height', 'auto');
            if ((submenuOffsetTop + submenu.outerHeight()) > win.height()) {
                submenuCSS.overflow = 'auto';
                submenuCSS.height = win.height() - (submenuOffsetTop + 20);
            }

            submenu.css(submenuCSS).show();
        }
    });

});

/*
 * PrimeUI SlideMenu Widget
 */

$(function () {

    $.widget("primeui.puislidemenu", $.primeui.puibasemenu, {

        _create: function () {

            this._render();

            //elements
            this.rootList = this.element;
            this.content = this.element.parent();
            this.wrapper = this.content.parent();
            this.container = this.wrapper.parent();
            this.submenus = this.container.find('ul.pui-menu-list');

            this.links = this.element.find('a.pui-menuitem-link:not(.ui-state-disabled)');
            this.backward = this.wrapper.children('div.pui-slidemenu-backward');

            //config
            this.stack = [];
            this.jqWidth = this.container.width();

            var $this = this;

            if (!this.element.hasClass('pui-menu-dynamic')) {
                this._applyDimensions();
            }
            this._super();

            this._bindEvents();
        },

        _render: function () {
            this.element.addClass('pui-menu-list ui-helper-reset').
                wrap('<div class="pui-menu pui-slidemenu ui-widget ui-widget-content ui-corner-all ui-helper-clearfix"/>').
                wrap('<div class="pui-slidemenu-wrapper" />').
                after('<div class="pui-slidemenu-backward ui-widget-header ui-corner-all ui-helper-clearfix">\n\
                    <span class="ui-icon ui-icon-triangle-1-w"></span>Back</div>').
                wrap('<div class="pui-slidemenu-content" />');

            this.element.parent().uniqueId();
            this.options.id = this.element.parent().attr('id');

            this.element.find('li').each(function () {
                var listItem = $(this),
                    menuitemLink = listItem.children('a'),
                    icon = menuitemLink.data('icon');

                menuitemLink.addClass('pui-menuitem-link ui-corner-all').contents().wrap('<span class="ui-menuitem-text" />');

                if (icon) {
                    menuitemLink.prepend('<span class="pui-menuitem-icon ui-icon ' + icon + '"></span>');
                }

                listItem.addClass('pui-menuitem ui-widget ui-corner-all');
                if (listItem.children('ul').length > 0) {
                    listItem.addClass('pui-menu-parent');
                    listItem.children('ul').addClass('ui-widget-content pui-menu-list ui-corner-all ui-helper-clearfix pui-menu-child ui-shadow');
                    menuitemLink.prepend('<span class="ui-icon ui-icon-triangle-1-e"></span>');
                }


            });
        },

        _bindEvents: function () {
            var $this = this;

            this.links.on('mouseenter.pui-menu', function () {
                $(this).addClass('ui-state-hover');
            })
                .on('mouseleave.pui-menu', function () {
                    $(this).removeClass('ui-state-hover');
                })
                .on('click.pui-menu', function () {
                    var link = $(this),
                        submenu = link.next();

                    if (submenu.length == 1) {
                        $this._forward(submenu)
                    }
                });

            this.backward.on('click.pui-menu', function () {
                $this._back();
            });
        },

        _forward: function (submenu) {
            var $this = this;

            this._push(submenu);

            var rootLeft = -1 * (this._depth() * this.jqWidth);

            submenu.show().css({
                left: this.jqWidth
            });

            this.rootList.animate({
                left: rootLeft
            }, 500, 'easeInOutCirc', function () {
                if ($this.backward.is(':hidden')) {
                    $this.backward.fadeIn('fast');
                }
            });
        },

        _back: function () {
            var $this = this,
                last = this._pop(),
                depth = this._depth();

            var rootLeft = -1 * (depth * this.jqWidth);

            this.rootList.animate({
                left: rootLeft
            }, 500, 'easeInOutCirc', function () {
                last.hide();

                if (depth == 0) {
                    $this.backward.fadeOut('fast');
                }
            });
        },

        _push: function (submenu) {
            this.stack.push(submenu);
        },

        _pop: function () {
            return this.stack.pop();
        },

        _last: function () {
            return this.stack[this.stack.length - 1];
        },

        _depth: function () {
            return this.stack.length;
        },

        _applyDimensions: function () {
            this.submenus.width(this.container.width());
            this.wrapper.height(this.rootList.outerHeight(true) + this.backward.outerHeight(true));
            this.content.height(this.rootList.outerHeight(true));
            this.rendered = true;
        },

        show: function () {
            this.align();
            this.container.css('z-index', ++PUI.zindex).show();

            if (!this.rendered) {
                this._applyDimensions();
            }
        }
    });

});


/**
 * PrimeUI Context Menu Widget
 */

$(function () {

    $.widget("primeui.puicontextmenu", $.primeui.puitieredmenu, {

        options: {
            autoDisplay: true,
            target: null,
            event: 'contextmenu'
        },

        _create: function () {
            this._super();
            this.element.parent().removeClass('pui-tieredmenu').
                addClass('pui-contextmenu pui-menu-dynamic pui-shadow');

            var $this = this;

            this.options.target = this.options.target || $(document);

            if (!this.element.parent().parent().is(document.body)) {
                this.element.parent().appendTo('body');
            }

            this.options.target.on(this.options.event + '.pui-contextmenu', function (e) {
                $this.show(e);
            });
        },

        _bindItemEvents: function () {
            this._super();

            var $this = this;

            //hide menu on item click
            this.links.bind('click', function () {
                $this._hide();
            });
        },

        _bindDocumentHandler: function () {
            var $this = this;

            //hide overlay when document is clicked
            $(document.body).bind('click.pui-contextmenu', function (e) {
                if ($this.element.parent().is(":hidden")) {
                    return;
                }

                $this._hide();
            });
        },

        show: function (e) {
            //hide other contextmenus if any
            $(document.body).children('.pui-contextmenu:visible').hide();

            var win = $(window),
                left = e.pageX,
                top = e.pageY,
                width = this.element.parent().outerWidth(),
                height = this.element.parent().outerHeight();

            //collision detection for window boundaries
            if ((left + width) > (win.width()) + win.scrollLeft()) {
                left = left - width;
            }
            if ((top + height ) > (win.height() + win.scrollTop())) {
                top = top - height;
            }

            if (this.options.beforeShow) {
                this.options.beforeShow.call(this);
            }

            this.element.parent().css({
                'left': left,
                'top': top,
                'z-index': ++PUI.zindex
            }).show();

            e.preventDefault();
            e.stopPropagation();
        },

        _hide: function () {
            var $this = this;

            //hide submenus
            this.element.parent().find('li.pui-menuitem-active').each(function () {
                $this._deactivate($(this), true);
            });

            this.element.parent().fadeOut('fast');
        },

        isVisible: function () {
            return this.element.parent().is(':visible');
        },

        getTarget: function () {
            return this.jqTarget;
        }

    });

});
/**
 * PrimeFaces Notify Widget
 */
$(function () {

    $.widget("primeui.puinotify", {

        options: {
            position: 'top',
            visible: false,
            animate: true,
            effectSpeed: 'normal',
            easing: 'swing'
        },

        _create: function () {
            this.element.addClass('pui-notify pui-notify-' + this.options.position + ' ui-widget ui-widget-content pui-shadow')
                .wrapInner('<div class="pui-notify-content" />').appendTo(document.body);
            this.content = this.element.children('.pui-notify-content');
            this.closeIcon = $('<span class="ui-icon ui-icon-closethick pui-notify-close"></span>').appendTo(this.element);

            this._bindEvents();

            if (this.options.visible) {
                this.show();
            }
        },

        _bindEvents: function () {
            var $this = this;

            this.closeIcon.on('click.puinotify', function () {
                $this.hide();
            });
        },

        show: function (content) {
            var $this = this;

            if (content) {
                this.update(content);
            }

            this.element.css('z-index', ++PUI.zindex);

            this._trigger('beforeShow');

            if (this.options.animate) {
                this.element.slideDown(this.options.effectSpeed, this.options.easing, function () {
                    $this._trigger('afterShow');
                });
            }
            else {
                this.element.show();
                $this._trigger('afterShow');
            }
        },

        hide: function () {
            var $this = this;

            this._trigger('beforeHide');

            if (this.options.animate) {
                this.element.slideUp(this.options.effectSpeed, this.options.easing, function () {
                    $this._trigger('afterHide');
                });
            }
            else {
                this.element.hide();
                $this._trigger('afterHide');
            }
        },

        update: function (content) {
            this.content.html(content);
        }
    });
});
/**
 * PrimeUI Paginator Widget
 */
$(function () {

    ElementHandlers = {

        '{FirstPageLink}': {
            markup: '<span class="pui-paginator-first pui-paginator-element ui-state-default ui-corner-all"><span class="ui-icon ui-icon-seek-first">p</span></span>',

            create: function (paginator) {
                var element = $(this.markup);

                if (paginator.options.page === 0) {
                    element.addClass('ui-state-disabled');
                }

                element.on('click.puipaginator', function () {
                    if (!$(this).hasClass("ui-state-disabled")) {
                        paginator.option('page', 0);
                    }
                });

                return element;
            },

            update: function (element, state) {
                if (state.page === 0)
                    element.addClass('ui-state-disabled').removeClass('ui-state-hover ui-state-active');
                else
                    element.removeClass('ui-state-disabled');
            }
        },

        '{PreviousPageLink}': {
            markup: '<span class="pui-paginator-prev pui-paginator-element ui-state-default ui-corner-all"><span class="ui-icon ui-icon-seek-prev">p</span></span>',

            create: function (paginator) {
                var element = $(this.markup);

                if (paginator.options.page === 0) {
                    element.addClass('ui-state-disabled');
                }

                element.on('click.puipaginator', function () {
                    if (!$(this).hasClass("ui-state-disabled")) {
                        paginator.option('page', paginator.options.page - 1);
                    }
                });

                return element;
            },

            update: function (element, state) {
                if (state.page === 0)
                    element.addClass('ui-state-disabled').removeClass('ui-state-hover ui-state-active');
                else
                    element.removeClass('ui-state-disabled');
            }
        },

        '{NextPageLink}': {
            markup: '<span class="pui-paginator-next pui-paginator-element ui-state-default ui-corner-all"><span class="ui-icon ui-icon-seek-next">p</span></span>',

            create: function (paginator) {
                var element = $(this.markup);

                if (paginator.options.page === (paginator.getPageCount() - 1)) {
                    element.addClass('ui-state-disabled').removeClass('ui-state-hover ui-state-active');
                }

                element.on('click.puipaginator', function () {
                    if (!$(this).hasClass("ui-state-disabled")) {
                        paginator.option('page', paginator.options.page + 1);
                    }
                });

                return element;
            },

            update: function (element, state) {
                if (state.page === (state.pageCount - 1))
                    element.addClass('ui-state-disabled').removeClass('ui-state-hover ui-state-active');
                else
                    element.removeClass('ui-state-disabled');
            }
        },

        '{LastPageLink}': {
            markup: '<span class="pui-paginator-last pui-paginator-element ui-state-default ui-corner-all"><span class="ui-icon ui-icon-seek-end">p</span></span>',

            create: function (paginator) {
                var element = $(this.markup);

                if (paginator.options.page === (paginator.getPageCount() - 1)) {
                    element.addClass('ui-state-disabled').removeClass('ui-state-hover ui-state-active');
                }

                element.on('click.puipaginator', function () {
                    if (!$(this).hasClass("ui-state-disabled")) {
                        paginator.option('page', paginator.getPageCount() - 1);
                    }
                });

                return element;
            },

            update: function (element, state) {
                if (state.page === (state.pageCount - 1))
                    element.addClass('ui-state-disabled').removeClass('ui-state-hover ui-state-active');
                else
                    element.removeClass('ui-state-disabled');
            }
        },

        '{PageLinks}': {
            markup: '<span class="pui-paginator-pages"></span>',

            create: function (paginator) {
                var element = $(this.markup),
                    boundaries = this.calculateBoundaries({
                        page: paginator.options.page,
                        pageLinks: paginator.options.pageLinks,
                        pageCount: paginator.getPageCount(),
                    }),
                    start = boundaries[0],
                    end = boundaries[1];

                for (var i = start; i <= end; i++) {
                    var pageLinkNumber = (i + 1),
                        pageLinkElement = $('<span class="pui-paginator-page pui-paginator-element ui-state-default ui-corner-all">' + pageLinkNumber + "</span>");

                    if (i === paginator.options.page) {
                        pageLinkElement.addClass('ui-state-active');
                    }

                    pageLinkElement.on('click.puipaginator', function (e) {
                        var link = $(this);

                        if (!link.hasClass('ui-state-disabled') && !link.hasClass('ui-state-active')) {
                            paginator.option('page', parseInt(link.text()) - 1);
                        }
                    });

                    element.append(pageLinkElement);
                }

                return element;
            },

            update: function (element, state) {
                var pageLinks = element.children(),
                    boundaries = this.calculateBoundaries({
                        page: state.page,
                        pageLinks: state.pageLinks,
                        pageCount: state.pageCount,
                    }),
                    start = boundaries[0],
                    end = boundaries[1],
                    p = 0;

                pageLinks.filter('.ui-state-active').removeClass('ui-state-active');

                for (var i = start; i <= end; i++) {
                    var pageLinkNumber = (i + 1),
                        pageLink = pageLinks.eq(p);

                    if (i === state.page) {
                        pageLink.addClass('ui-state-active');
                    }

                    pageLink.text(pageLinkNumber);

                    p++;
                }
            },

            calculateBoundaries: function (config) {
                var page = config.page,
                    pageLinks = config.pageLinks,
                    pageCount = config.pageCount,
                    visiblePages = Math.min(pageLinks, pageCount);

                //calculate range, keep current in middle if necessary
                var start = Math.max(0, parseInt(Math.ceil(page - ((visiblePages) / 2)))),
                    end = Math.min(pageCount - 1, start + visiblePages - 1);

                //check when approaching to last page
                var delta = pageLinks - (end - start + 1);
                start = Math.max(0, start - delta);

                return [start, end];
            }
        }

    };

    $.widget("primeui.puipaginator", {

        options: {
            pageLinks: 5,
            totalRecords: 0,
            page: 0,
            rows: 0,
            template: '{FirstPageLink} {PreviousPageLink} {PageLinks} {NextPageLink} {LastPageLink}'
        },

        _create: function () {
            this.element.addClass('pui-paginator ui-widget-header');
            this.paginatorElements = [];

            var elementKeys = this.options.template.split(/[ ]+/);
            for (var i = 0; i < elementKeys.length; i++) {
                var elementKey = elementKeys[i],
                    handler = ElementHandlers[elementKey];

                if (handler) {
                    var paginatorElement = handler.create(this);
                    this.paginatorElements[elementKey] = paginatorElement;
                    this.element.append(paginatorElement);
                }
            }

            this._bindEvents();
        },

        _bindEvents: function () {
            this.element.find('span.pui-paginator-element')
                .on('mouseover.puipaginator', function () {
                    var el = $(this);
                    if (!el.hasClass('ui-state-active') && !el.hasClass('ui-state-disabled')) {
                        el.addClass('ui-state-hover');
                    }
                })
                .on('mouseout.puipaginator', function () {
                    var el = $(this);
                    if (el.hasClass('ui-state-hover')) {
                        el.removeClass('ui-state-hover');
                    }
                });
        },

        _setOption: function (key, value) {
            if (key === 'page') {
                this.setPage(value);
            }
            else {
                $.Widget.prototype._setOption.apply(this, arguments);
            }
        },

        setPage: function (p, silent) {
            var pc = this.getPageCount();

            if (p >= 0 && p < pc && this.options.page !== p) {
                var newState = {
                    first: this.options.rows * p,
                    rows: this.options.rows,
                    page: p,
                    pageCount: pc,
                    pageLinks: this.options.pageLinks
                };

                this.options.page = p;

                if (!silent) {
                    this._trigger('paginate', null, newState);
                }

                this.updateUI(newState);
            }
        },

        updateUI: function (state) {
            for (var paginatorElementKey in this.paginatorElements) {
                ElementHandlers[paginatorElementKey].update(this.paginatorElements[paginatorElementKey], state);
            }
        },

        getPageCount: function () {
            return Math.ceil(this.options.totalRecords / this.options.rows) || 1;
        }
    });
});
/**
 * PrimeUI Panel Widget
 */
$(function () {

    $.widget("primeui.puipanel", {

        options: {
            toggleable: false,
            toggleDuration: 'normal',
            toggleOrientation: 'vertical',
            collapsed: false,
            closable: false,
            closeDuration: 'normal'
        },

        _create: function () {
            this.element.addClass('pui-panel ui-widget ui-widget-content ui-corner-all')
                .contents().wrapAll('<div class="pui-panel-content ui-widget-content" />');

            var title = this.element.attr('title');
            if (title) {
                this.element.prepend('<div class="pui-panel-titlebar ui-widget-header ui-helper-clearfix ui-corner-all"><span class="ui-panel-title">'
                        + title + "</span></div>")
                    .removeAttr('title');
            }

            this.header = this.element.children('div.pui-panel-titlebar');
            this.title = this.header.children('span.ui-panel-title');
            this.content = this.element.children('div.pui-panel-content');

            var $this = this;

            if (this.options.closable) {
                this.closer = $('<a class="pui-panel-titlebar-icon ui-corner-all ui-state-default" href="#"><span class="ui-icon ui-icon-closethick"></span></a>')
                    .appendTo(this.header)
                    .on('click.puipanel', function (e) {
                        $this.close();
                        e.preventDefault();
                    });
            }

            if (this.options.toggleable) {
                var icon = this.options.collapsed ? 'ui-icon-plusthick' : 'ui-icon-minusthick';

                this.toggler = $('<a class="pui-panel-titlebar-icon ui-corner-all ui-state-default" href="#"><span class="ui-icon ' + icon + '"></span></a>')
                    .appendTo(this.header)
                    .on('click.puipanel', function (e) {
                        $this.toggle();
                        e.preventDefault();
                    });

                if (this.options.collapsed) {
                    this.content.hide();
                }
            }

            this._bindEvents();
        },

        _bindEvents: function () {
            this.header.find('a.pui-panel-titlebar-icon').on('hover.puipanel', function () {
                $(this).toggleClass('ui-state-hover');
            });
        },

        close: function () {
            var $this = this;

            this._trigger('beforeClose', null);

            this.element.fadeOut(this.options.closeDuration,
                function () {
                    $this._trigger('afterClose', null);
                }
            );
        },

        toggle: function () {
            if (this.options.collapsed) {
                this.expand();
            }
            else {
                this.collapse();
            }
        },

        expand: function () {
            this.toggler.children('span.ui-icon').removeClass('ui-icon-plusthick').addClass('ui-icon-minusthick');

            if (this.options.toggleOrientation === 'vertical') {
                this._slideDown();
            }
            else if (this.options.toggleOrientation === 'horizontal') {
                this._slideRight();
            }
        },

        collapse: function () {
            this.toggler.children('span.ui-icon').removeClass('ui-icon-minusthick').addClass('ui-icon-plusthick');

            if (this.options.toggleOrientation === 'vertical') {
                this._slideUp();
            }
            else if (this.options.toggleOrientation === 'horizontal') {
                this._slideLeft();
            }
        },

        _slideUp: function () {
            var $this = this;

            this._trigger('beforeCollapse');

            this.content.slideUp(this.options.toggleDuration, 'easeInOutCirc', function () {
                $this._trigger('afterCollapse');
                $this.options.collapsed = !$this.options.collapsed;
            });
        },

        _slideDown: function () {
            var $this = this;

            this._trigger('beforeExpand');

            this.content.slideDown(this.options.toggleDuration, 'easeInOutCirc', function () {
                $this._trigger('afterExpand');
                $this.options.collapsed = !$this.options.collapsed;
            });
        },

        _slideLeft: function () {
            var $this = this;

            this.originalWidth = this.element.width();

            this.title.hide();
            this.toggler.hide();
            this.content.hide();

            this.element.animate({
                width: '42px'
            }, this.options.toggleSpeed, 'easeInOutCirc', function () {
                $this.toggler.show();
                $this.element.addClass('pui-panel-collapsed-h');
                $this.options.collapsed = !$this.options.collapsed;
            });
        },

        _slideRight: function () {
            var $this = this,
                expandWidth = this.originalWidth || '100%';

            this.toggler.hide();

            this.element.animate({
                width: expandWidth
            }, this.options.toggleSpeed, 'easeInOutCirc', function () {
                $this.element.removeClass('pui-panel-collapsed-h');
                $this.title.show();
                $this.toggler.show();
                $this.options.collapsed = !$this.options.collapsed;

                $this.content.css({
                    'visibility': 'visible', 'display': 'block', 'height': 'auto'
                });
            });
        }
    });
});
/**
 * PrimeUI password widget
 */
$(function () {

    $.widget("primeui.puipassword", {

        options: {
            promptLabel: 'Please enter a password',
            weakLabel: 'Weak',
            goodLabel: 'Medium',
            strongLabel: 'Strong',
            inline: false
        },

        _create: function () {
            this.element.puiinputtext().addClass('pui-password');

            if (!this.element.prop(':disabled')) {
                var panelMarkup = '<div class="pui-password-panel ui-widget ui-state-highlight ui-corner-all ui-helper-hidden">';
                panelMarkup += '<div class="pui-password-meter" style="background-position:0pt 0pt">&nbsp;</div>';
                panelMarkup += '<div class="pui-password-info">' + this.options.promptLabel + '</div>';
                panelMarkup += '</div>';

                this.panel = $(panelMarkup).insertAfter(this.element);
                this.meter = this.panel.children('div.pui-password-meter');
                this.infoText = this.panel.children('div.pui-password-info');

                if (this.options.inline) {
                    this.panel.addClass('pui-password-panel-inline');
                } else {
                    this.panel.addClass('pui-password-panel-overlay').appendTo('body');
                }

                this._bindEvents();
            }
        },

        _destroy: function () {
            this.panel.remove();
        },

        _bindEvents: function () {
            var $this = this;

            this.element.on('focus.puipassword', function () {
                $this.show();
            })
                .on('blur.puipassword', function () {
                    $this.hide();
                })
                .on('keyup.puipassword', function () {
                    var value = $this.element.val(),
                        label = null,
                        meterPos = null;

                    if (value.length == 0) {
                        label = $this.options.promptLabel;
                        meterPos = '0px 0px';
                    }
                    else {
                        var score = $this._testStrength($this.element.val());

                        if (score < 30) {
                            label = $this.options.weakLabel;
                            meterPos = '0px -10px';
                        }
                        else if (score >= 30 && score < 80) {
                            label = $this.options.goodLabel;
                            meterPos = '0px -20px';
                        }
                        else if (score >= 80) {
                            label = $this.options.strongLabel;
                            meterPos = '0px -30px';
                        }
                    }

                    $this.meter.css('background-position', meterPos);
                    $this.infoText.text(label);
                });

            if (!this.options.inline) {
                var resizeNS = 'resize.' + this.element.attr('id');
                $(window).unbind(resizeNS).bind(resizeNS, function () {
                    if ($this.panel.is(':visible')) {
                        $this.align();
                    }
                });
            }
        },

        _testStrength: function (str) {
            var grade = 0,
                val = 0,
                $this = this;

            val = str.match('[0-9]');
            grade += $this._normalize(val ? val.length : 1 / 4, 1) * 25;

            val = str.match('[a-zA-Z]');
            grade += $this._normalize(val ? val.length : 1 / 2, 3) * 10;

            val = str.match('[!@#$%^&*?_~.,;=]');
            grade += $this._normalize(val ? val.length : 1 / 6, 1) * 35;

            val = str.match('[A-Z]');
            grade += $this._normalize(val ? val.length : 1 / 6, 1) * 30;

            grade *= str.length / 8;

            return grade > 100 ? 100 : grade;
        },

        _normalize: function (x, y) {
            var diff = x - y;

            if (diff <= 0) {
                return x / y;
            }
            else {
                return 1 + 0.5 * (x / (x + y / 4));
            }
        },

        align: function () {
            this.panel.css({
                left: '',
                top: '',
                'z-index': ++PUI.zindex
            })
                .position({
                    my: 'left top',
                    at: 'right top',
                    of: this.element
                });
        },

        show: function () {
            if (!this.options.inline) {
                this.align();

                this.panel.fadeIn();
            }
            else {
                this.panel.slideDown();
            }
        },

        hide: function () {
            if (this.options.inline)
                this.panel.slideUp();
            else
                this.panel.fadeOut();
        }
    });

});
/**
 * PrimeUI picklist widget
 */
$(function () {

    $.widget("primeui.puipicklist", {

        options: {
            effect: 'fade',
            effectSpeed: 'fast',
            sourceCaption: null,
            targetCaption: null,
            filter: false,
            filterFunction: null,
            filterMatchMode: 'startsWith',
            dragdrop: true,
            sourceData: null,
            targetData: null,
            content: null
        },

        _create: function () {
            this.element.uniqueId().addClass('pui-picklist ui-widget ui-helper-clearfix');
            this.inputs = this.element.children('select');
            this.items = $();
            this.sourceInput = this.inputs.eq(0);
            this.targetInput = this.inputs.eq(1);

            if (this.options.sourceData) {
                this._populateInputFromData(this.sourceInput, this.options.sourceData);
            }

            if (this.options.targetData) {
                this._populateInputFromData(this.targetInput, this.options.targetData);
            }

            this.sourceList = this._createList(this.sourceInput, 'pui-picklist-source', this.options.sourceCaption, this.options.sourceData);
            this._createButtons();
            this.targetList = this._createList(this.targetInput, 'pui-picklist-target', this.options.targetCaption, this.options.targetData);

            if (this.options.showSourceControls) {
                this.element.prepend(this._createListControls(this.sourceList));
            }

            if (this.options.showTargetControls) {
                this.element.append(this._createListControls(this.targetList));
            }

            this._bindEvents();
        },

        _populateInputFromData: function (input, data) {
            for (var i = 0; i < data.length; i++) {
                var choice = data[i];
                if (choice.label)
                    input.append('<option value="' + choice.value + '">' + choice.label + '</option>');
                else
                    input.append('<option value="' + choice + '">' + choice + '</option>');
            }
        },

        _createList: function (input, cssClass, caption, data) {
            input.wrap('<div class="ui-helper-hidden"></div>');

            var listWrapper = $('<div class="pui-picklist-listwrapper ' + cssClass + '"></div>'),
                listContainer = $('<ul class="ui-widget-content pui-picklist-list pui-inputtext"></ul>'),
                choices = input.children('option');

            if (this.options.filter) {
                listWrapper.append('<div class="pui-picklist-filter-container"><input type="text" class="pui-picklist-filter" /><span class="ui-icon ui-icon-search"></span></div>');
                listWrapper.find('> .pui-picklist-filter-container > input').puiinputtext();
            }

            if (caption) {
                listWrapper.append('<div class="pui-picklist-caption ui-widget-header ui-corner-tl ui-corner-tr">' + caption + '</div>');
                listContainer.addClass('ui-corner-bottom');
            }
            else {
                listContainer.addClass('ui-corner-all');
            }

            for (var i = 0; i < choices.length; i++) {
                var choice = choices.eq(i),
                    content = this.options.content ? this.options.content.call(this, data[i]) : choice.text(),
                    item = $('<li class="pui-picklist-item ui-corner-all">' + content + '</li>').data({
                        'item-label': choice.text(),
                        'item-value': choice.val()
                    });

                this.items = this.items.add(item);
                listContainer.append(item);
            }

            listWrapper.append(listContainer).appendTo(this.element);

            return listContainer;
        },

        _createButtons: function () {
            var $this = this,
                buttonContainer = $('<ul class="pui-picklist-buttons"></ul>');

            buttonContainer.append(this._createButton('ui-icon-arrow-1-e', 'pui-picklist-button-add', function () {
                    $this._add();
                }))
                .append(this._createButton('ui-icon-arrowstop-1-e', 'pui-picklist-button-addall', function () {
                    $this._addAll();
                }))
                .append(this._createButton('ui-icon-arrow-1-w', 'pui-picklist-button-remove', function () {
                    $this._remove();
                }))
                .append(this._createButton('ui-icon-arrowstop-1-w', 'pui-picklist-button-removeall', function () {
                    $this._removeAll();
                }));

            this.element.append(buttonContainer);
        },

        _createListControls: function (list) {
            var $this = this,
                buttonContainer = $('<ul class="pui-picklist-buttons"></ul>');

            buttonContainer.append(this._createButton('ui-icon-arrow-1-n', 'pui-picklist-button-move-up', function () {
                    $this._moveUp(list);
                }))
                .append(this._createButton('ui-icon-arrowstop-1-n', 'pui-picklist-button-move-top', function () {
                    $this._moveTop(list);
                }))
                .append(this._createButton('ui-icon-arrow-1-s', 'pui-picklist-button-move-down', function () {
                    $this._moveDown(list);
                }))
                .append(this._createButton('ui-icon-arrowstop-1-s', 'pui-picklist-button-move-bottom', function () {
                    $this._moveBottom(list);
                }));

            return buttonContainer;
        },

        _createButton: function (icon, cssClass, fn) {
            var btn = $('<button class="' + cssClass + '" type="button"></button>').puibutton({
                'icon': icon,
                'click': fn
            });

            return btn;
        },

        _bindEvents: function () {
            var $this = this;

            this.items.on('mouseover.puipicklist', function (e) {
                var element = $(this);

                if (!element.hasClass('ui-state-highlight')) {
                    $(this).addClass('ui-state-hover');
                }
            })
                .on('mouseout.puipicklist', function (e) {
                    $(this).removeClass('ui-state-hover');
                })
                .on('click.puipicklist', function (e) {
                    var item = $(this),
                        metaKey = (e.metaKey || e.ctrlKey);

                    if (!e.shiftKey) {
                        if (!metaKey) {
                            $this.unselectAll();
                        }

                        if (metaKey && item.hasClass('ui-state-highlight')) {
                            $this.unselectItem(item);
                        }
                        else {
                            $this.selectItem(item);
                            $this.cursorItem = item;
                        }
                    }
                    else {
                        $this.unselectAll();

                        if ($this.cursorItem && ($this.cursorItem.parent().is(item.parent()))) {
                            var currentItemIndex = item.index(),
                                cursorItemIndex = $this.cursorItem.index(),
                                startIndex = (currentItemIndex > cursorItemIndex) ? cursorItemIndex : currentItemIndex,
                                endIndex = (currentItemIndex > cursorItemIndex) ? (currentItemIndex + 1) : (cursorItemIndex + 1),
                                parentList = item.parent();

                            for (var i = startIndex; i < endIndex; i++) {
                                $this.selectItem(parentList.children('li.ui-picklist-item').eq(i));
                            }
                        }
                        else {
                            $this.selectItem(item);
                            $this.cursorItem = item;
                        }
                    }
                })
                .on('dblclick.pickList', function () {
                    var item = $(this);

                    if ($(this).closest('.pui-picklist-listwrapper').hasClass('pui-picklist-source'))
                        $this._transfer(item, $this.sourceList, $this.targetList, 'dblclick');
                    else
                        $this._transfer(item, $this.targetList, $this.sourceList, 'dblclick');

                    PUI.clearSelection();
                });

            if (this.options.filter) {
                this._setupFilterMatcher();

                this.element.find('> .pui-picklist-source > .pui-picklist-filter-container > input').on('keyup', function (e) {
                    $this._filter(this.value, $this.sourceList);
                });

                this.element.find('> .pui-picklist-target > .pui-picklist-filter-container > input').on('keyup', function (e) {
                    $this._filter(this.value, $this.targetList);
                });
            }

            if (this.options.dragdrop) {
                this.element.find('> .pui-picklist-listwrapper > ul.pui-picklist-list').sortable({
                    cancel: '.ui-state-disabled',
                    connectWith: '#' + this.element.attr('id') + ' .pui-picklist-list',
                    revert: true,
                    containment: this.element,
                    update: function (event, ui) {
                        $this.unselectItem(ui.item);

                        $this._saveState();
                    },
                    receive: function (event, ui) {
                        $this._triggerTransferEvent(ui.item, ui.sender, ui.item.closest('ul.pui-picklist-list'), 'dragdrop');
                    }
                });
            }
        },

        selectItem: function (item) {
            item.removeClass('ui-state-hover').addClass('ui-state-highlight');
        },

        unselectItem: function (item) {
            item.removeClass('ui-state-highlight');
        },

        unselectAll: function () {
            var selectedItems = this.items.filter('.ui-state-highlight');
            for (var i = 0; i < selectedItems.length; i++) {
                this.unselectItem(selectedItems.eq(i));
            }
        },

        _add: function () {
            var items = this.sourceList.children('li.pui-picklist-item.ui-state-highlight')

            this._transfer(items, this.sourceList, this.targetList, 'command');
        },

        _addAll: function () {
            var items = this.sourceList.children('li.pui-picklist-item:visible:not(.ui-state-disabled)');

            this._transfer(items, this.sourceList, this.targetList, 'command');
        },

        _remove: function () {
            var items = this.targetList.children('li.pui-picklist-item.ui-state-highlight');

            this._transfer(items, this.targetList, this.sourceList, 'command');
        },

        _removeAll: function () {
            var items = this.targetList.children('li.pui-picklist-item:visible:not(.ui-state-disabled)');

            this._transfer(items, this.targetList, this.sourceList, 'command');
        },

        _moveUp: function (list) {
            var $this = this,
                animated = $this.options.effect,
                items = list.children('.ui-state-highlight'),
                itemsCount = items.length,
                movedCount = 0;

            items.each(function () {
                var item = $(this);

                if (!item.is(':first-child')) {
                    if (animated) {
                        item.hide($this.options.effect, {}, $this.options.effectSpeed, function () {
                            item.insertBefore(item.prev()).show($this.options.effect, {}, $this.options.effectSpeed, function () {
                                movedCount++;

                                if (movedCount === itemsCount) {
                                    $this._saveState();
                                }
                            });
                        });
                    }
                    else {
                        item.hide().insertBefore(item.prev()).show();
                    }

                }
            });

            if (!animated) {
                this._saveState();
            }

        },

        _moveTop: function (list) {
            var $this = this,
                animated = $this.options.effect,
                items = list.children('.ui-state-highlight'),
                itemsCount = items.length,
                movedCount = 0;

            list.children('.ui-state-highlight').each(function () {
                var item = $(this);

                if (!item.is(':first-child')) {
                    if (animated) {
                        item.hide($this.options.effect, {}, $this.options.effectSpeed, function () {
                            item.prependTo(item.parent()).show($this.options.effect, {}, $this.options.effectSpeed, function () {
                                movedCount++;

                                if (movedCount === itemsCount) {
                                    $this._saveState();
                                }
                            });
                        });
                    }
                    else {
                        item.hide().prependTo(item.parent()).show();
                    }
                }
            });

            if (!animated) {
                this._saveState();
            }
        },

        _moveDown: function (list) {
            var $this = this,
                animated = $this.options.effect,
                items = list.children('.ui-state-highlight'),
                itemsCount = items.length,
                movedCount = 0;

            $(list.children('.ui-state-highlight').get().reverse()).each(function () {
                var item = $(this);

                if (!item.is(':last-child')) {
                    if (animated) {
                        item.hide($this.options.effect, {}, $this.options.effectSpeed, function () {
                            item.insertAfter(item.next()).show($this.options.effect, {}, $this.options.effectSpeed, function () {
                                movedCount++;

                                if (movedCount === itemsCount) {
                                    $this._saveState();
                                }
                            });
                        });
                    }
                    else {
                        item.hide().insertAfter(item.next()).show();
                    }
                }

            });

            if (!animated) {
                this._saveState();
            }
        },

        _moveBottom: function (list) {
            var $this = this,
                animated = $this.options.effect,
                items = list.children('.ui-state-highlight'),
                itemsCount = items.length,
                movedCount = 0;

            list.children('.ui-state-highlight').each(function () {
                var item = $(this);

                if (!item.is(':last-child')) {

                    if (animated) {
                        item.hide($this.options.effect, {}, $this.options.effectSpeed, function () {
                            item.appendTo(item.parent()).show($this.options.effect, {}, $this.options.effectSpeed, function () {
                                movedCount++;

                                if (movedCount === itemsCount) {
                                    $this._saveState();
                                }
                            });
                        });
                    }
                    else {
                        item.hide().appendTo(item.parent()).show();
                    }
                }

            });

            if (!animated) {
                this._saveState();
            }
        },

        _transfer: function (items, from, to, type) {
            var $this = this,
                itemsCount = items.length,
                transferCount = 0;

            if (this.options.effect) {
                items.hide(this.options.effect, {}, this.options.effectSpeed, function () {
                    var item = $(this);
                    $this.unselectItem(item);

                    item.appendTo(to).show($this.options.effect, {}, $this.options.effectSpeed, function () {
                        transferCount++;

                        if (transferCount === itemsCount) {
                            $this._saveState();
                            $this._triggerTransferEvent(items, from, to, type);
                        }
                    });
                });
            }
            else {
                items.hide().removeClass('ui-state-highlight ui-state-hover').appendTo(to).show();

                this._saveState();
                this._triggerTransferEvent(items, from, to, type);
            }
        },

        _triggerTransferEvent: function (items, from, to, type) {
            var obj = {};
            obj.items = items;
            obj.from = from;
            obj.to = to;
            obj.type = type;

            this._trigger('transfer', null, obj);
        },

        _saveState: function () {
            this.sourceInput.children().remove();
            this.targetInput.children().remove();

            this._generateItems(this.sourceList, this.sourceInput);
            this._generateItems(this.targetList, this.targetInput);
            this.cursorItem = null;
        },

        _generateItems: function (list, input) {
            list.children('.pui-picklist-item').each(function () {
                var item = $(this),
                    itemValue = item.data('item-value'),
                    itemLabel = item.data('item-label');

                input.append('<option value="' + itemValue + '" selected="selected">' + itemLabel + '</option>');
            });
        },

        _setupFilterMatcher: function () {
            this.filterMatchers = {
                'startsWith': this._startsWithFilter, 'contains': this._containsFilter, 'endsWith': this._endsWithFilter, 'custom': this.options.filterFunction
            };

            this.filterMatcher = this.filterMatchers[this.options.filterMatchMode];
        },

        _filter: function (value, list) {
            var filterValue = $.trim(value).toLowerCase(),
                items = list.children('li.pui-picklist-item');

            if (filterValue === '') {
                items.filter(':hidden').show();
            }
            else {
                for (var i = 0; i < items.length; i++) {
                    var item = items.eq(i),
                        itemLabel = item.data('item-label');

                    if (this.filterMatcher(itemLabel, filterValue))
                        item.show();
                    else
                        item.hide();
                }
            }
        },

        _startsWithFilter: function (value, filter) {
            return value.toLowerCase().indexOf(filter) === 0;
        },

        _containsFilter: function (value, filter) {
            return value.toLowerCase().indexOf(filter) !== -1;
        },

        _endsWithFilter: function (value, filter) {
            return value.indexOf(filter, value.length - filter.length) !== -1;
        },
    });

});
/**
 * PrimeUI progressbar widget
 */
$(function () {

    $.widget("primeui.puiprogressbar", {

        options: {
            value: 0,
            labelTemplate: '{value}%',
            complete: null,
            easing: 'easeInOutCirc',
            effectSpeed: 'normal',
            showLabel: true
        },

        _create: function () {
            this.element.addClass('pui-progressbar ui-widget ui-widget-content ui-corner-all')
                .append('<div class="pui-progressbar-value ui-widget-header ui-corner-all"></div>')
                .append('<div class="pui-progressbar-label"></div>');

            this.jqValue = this.element.children('.pui-progressbar-value');
            this.jqLabel = this.element.children('.pui-progressbar-label');

            if (this.options.value !== 0) {
                this._setValue(this.options.value, false);
            }

            this.enableARIA();
        },

        _setValue: function (value, animate) {
            var anim = (animate === undefined || animate) ? true : false;

            if (value >= 0 && value <= 100) {
                if (value === 0) {
                    this.jqValue.hide().css('width', '0%').removeClass('ui-corner-right');

                    this.jqLabel.hide();
                }
                else {
                    if (anim) {
                        this.jqValue.show().animate({
                            'width': value + '%'
                        }, this.options.effectSpeed, this.options.easing);
                    }
                    else {
                        this.jqValue.show().css('width', value + '%');
                    }

                    if (this.options.labelTemplate && this.options.showLabel) {
                        var formattedLabel = this.options.labelTemplate.replace(/{value}/gi, value);

                        this.jqLabel.html(formattedLabel).show();
                    }

                    if (value === 100) {
                        this._trigger('complete');
                    }
                }

                this.options.value = value;
                this.element.attr('aria-valuenow', value);
            }
        },

        _getValue: function () {
            return this.options.value;
        },

        enableARIA: function () {
            this.element.attr('role', 'progressbar')
                .attr('aria-valuemin', 0)
                .attr('aria-valuenow', this.options.value)
                .attr('aria-valuemax', 100);
        },

        _setOption: function (key, value) {
            if (key === 'value') {
                this._setValue(value);
            }

            $.Widget.prototype._setOption.apply(this, arguments);
        },

        _destroy: function () {

        }

    });

});
/**
 * PrimeUI radiobutton widget
 */
$(function () {

    var checkedRadios = {};

    $.widget("primeui.puiradiobutton", {

        _create: function () {
            this.element.wrap('<div class="pui-radiobutton ui-widget"><div class="ui-helper-hidden-accessible"></div></div>');
            this.container = this.element.parent().parent();
            this.box = $('<div class="pui-radiobutton-box ui-widget pui-radiobutton-relative ui-state-default">').appendTo(this.container);
            this.icon = $('<span class="pui-radiobutton-icon pui-c"></span>').appendTo(this.box);
            this.disabled = this.element.prop('disabled');
            this.label = $('label[for="' + this.element.attr('id') + '"]');

            if (this.element.prop('checked')) {
                this.box.addClass('ui-state-active');
                this.icon.addClass('ui-icon ui-icon-bullet');
                checkedRadios[this.element.attr('name')] = this.box;
            }

            if (this.disabled) {
                this.box.addClass('ui-state-disabled');
            } else {
                this._bindEvents();
            }
        },

        _bindEvents: function () {
            var $this = this;

            this.box.on('mouseover.puiradiobutton',function () {
                if (!$this._isChecked())
                    $this.box.addClass('ui-state-hover');
            }).on('mouseout.puiradiobutton',function () {
                    if (!$this._isChecked())
                        $this.box.removeClass('ui-state-hover');
                }).on('click.puiradiobutton', function () {
                    if (!$this._isChecked()) {
                        $this.element.trigger('click');

                        if ($.browser.msie && parseInt($.browser.version) < 9) {
                            $this.element.trigger('change');
                        }
                    }
                });

            if (this.label.length > 0) {
                this.label.on('click.puiradiobutton', function (e) {
                    $this.element.trigger('click');

                    e.preventDefault();
                });
            }

            this.element.focus(function () {
                if ($this._isChecked()) {
                    $this.box.removeClass('ui-state-active');
                }

                $this.box.addClass('ui-state-focus');
            })
                .blur(function () {
                    if ($this._isChecked()) {
                        $this.box.addClass('ui-state-active');
                    }

                    $this.box.removeClass('ui-state-focus');
                })
                .change(function (e) {
                    var name = $this.element.attr('name');
                    if (checkedRadios[name]) {
                        checkedRadios[name].removeClass('ui-state-active ui-state-focus ui-state-hover').children('.pui-radiobutton-icon').removeClass('ui-icon ui-icon-bullet');
                    }

                    $this.icon.addClass('ui-icon ui-icon-bullet');
                    if (!$this.element.is(':focus')) {
                        $this.box.addClass('ui-state-active');
                    }

                    checkedRadios[name] = $this.box;

                    $this._trigger('change', null);
                });
        },

        _isChecked: function () {
            return this.element.prop('checked');
        }
    });

});
/**
 * PrimeUI rating widget
 */
$(function () {

    $.widget("primeui.puirating", {

        options: {
            stars: 5,
            cancel: true
        },

        _create: function () {
            var input = this.element;

            input.wrap('<div />');
            this.container = input.parent();
            this.container.addClass('pui-rating');

            var inputVal = input.val(),
                value = inputVal == '' ? null : parseInt(inputVal);

            if (this.options.cancel) {
                this.container.append('<div class="pui-rating-cancel"><a></a></div>');
            }

            for (var i = 0; i < this.options.stars; i++) {
                var styleClass = (value > i) ? "pui-rating-star pui-rating-star-on" : "pui-rating-star";

                this.container.append('<div class="' + styleClass + '"><a></a></div>');
            }

            this.stars = this.container.children('.pui-rating-star');

            if (input.prop('disabled')) {
                this.container.addClass('ui-state-disabled');
            }
            else if (!input.prop('readonly')) {
                this._bindEvents();
            }
        },

        _bindEvents: function () {
            var $this = this;

            this.stars.click(function () {
                var value = $this.stars.index(this) + 1;   //index starts from zero

                $this.setValue(value);
            });

            this.container.children('.pui-rating-cancel').hover(function () {
                $(this).toggleClass('pui-rating-cancel-hover');
            })
                .click(function () {
                    $this.cancel();
                });
        },

        cancel: function () {
            this.element.val('');

            this.stars.filter('.pui-rating-star-on').removeClass('pui-rating-star-on');

            this._trigger('cancel', null);
        },

        getValue: function () {
            var inputVal = this.element.val();

            return inputVal == '' ? null : parseInt(inputVal);
        },

        setValue: function (value) {
            this.element.val(value);

            //update visuals
            this.stars.removeClass('pui-rating-star-on');
            for (var i = 0; i < value; i++) {
                this.stars.eq(i).addClass('pui-rating-star-on');
            }

            this._trigger('rate', null, value);
        }
    });

});
/**
 * PrimeUI spinner widget
 */
$(function () {

    $.widget("primeui.puispinner", {

        options: {
            step: 1.0
        },

        _create: function () {
            var input = this.element,
                disabled = input.prop('disabled');

            input.puiinputtext().addClass('pui-spinner-input').wrap('<span class="pui-spinner ui-widget ui-corner-all" />');
            this.wrapper = input.parent();
            this.wrapper.append('<a class="pui-spinner-button pui-spinner-up ui-corner-tr ui-button ui-widget ui-state-default ui-button-text-only"><span class="ui-button-text"><span class="ui-icon ui-icon-triangle-1-n"></span></span></a><a class="pui-spinner-button pui-spinner-down ui-corner-br ui-button ui-widget ui-state-default ui-button-text-only"><span class="ui-button-text"><span class="ui-icon ui-icon-triangle-1-s"></span></span></a>');
            this.upButton = this.wrapper.children('a.pui-spinner-up');
            this.downButton = this.wrapper.children('a.pui-spinner-down');
            this.options.step = this.options.step || 1;

            if (parseInt(this.options.step) === 0) {
                this.options.precision = this.options.step.toString().split(/[,]|[.]/)[1].length;
            }

            this._initValue();

            if (!disabled && !input.prop('readonly')) {
                this._bindEvents();
            }

            if (disabled) {
                this.wrapper.addClass('ui-state-disabled');
            }

            //aria
            input.attr({
                'role': 'spinner', 'aria-multiline': false, 'aria-valuenow': this.value
            });

            if (this.options.min != undefined)
                input.attr('aria-valuemin', this.options.min);

            if (this.options.max != undefined)
                input.attr('aria-valuemax', this.options.max);

            if (input.prop('disabled'))
                input.attr('aria-disabled', true);

            if (input.prop('readonly'))
                input.attr('aria-readonly', true);
        },


        _bindEvents: function () {
            var $this = this;

            //visuals for spinner buttons
            this.wrapper.children('.pui-spinner-button')
                .mouseover(function () {
                    $(this).addClass('ui-state-hover');
                }).mouseout(function () {
                    $(this).removeClass('ui-state-hover ui-state-active');

                    if ($this.timer) {
                        clearInterval($this.timer);
                    }
                }).mouseup(function () {
                    clearInterval($this.timer);
                    $(this).removeClass('ui-state-active').addClass('ui-state-hover');
                }).mousedown(function (e) {
                    var element = $(this),
                        dir = element.hasClass('pui-spinner-up') ? 1 : -1;

                    element.removeClass('ui-state-hover').addClass('ui-state-active');

                    if ($this.element.is(':not(:focus)')) {
                        $this.element.focus();
                    }

                    $this._repeat(null, dir);

                    //keep focused
                    e.preventDefault();
                });

            this.element.keydown(function (e) {
                var keyCode = $.ui.keyCode;

                switch (e.which) {
                    case keyCode.UP:
                        $this._spin($this.options.step);
                        break;

                    case keyCode.DOWN:
                        $this._spin(-1 * $this.options.step);
                        break;

                    default:
                        //do nothing
                        break;
                }
            })
                .keyup(function () {
                    $this._updateValue();
                })
                .blur(function () {
                    $this._format();
                })
                .focus(function () {
                    //remove formatting
                    $this.element.val($this.value);
                });

            //mousewheel
            this.element.bind('mousewheel', function (event, delta) {
                if ($this.element.is(':focus')) {
                    if (delta > 0)
                        $this._spin($this.options.step);
                    else
                        $this._spin(-1 * $this.options.step);

                    return false;
                }
            });
        },

        _repeat: function (interval, dir) {
            var $this = this,
                i = interval || 500;

            clearTimeout(this.timer);
            this.timer = setTimeout(function () {
                $this._repeat(40, dir);
            }, i);

            this._spin(this.options.step * dir);
        },

        _toFixed: function (value, precision) {
            var power = Math.pow(10, precision || 0);
            return String(Math.round(value * power) / power);
        },

        _spin: function (step) {
            var newValue;
            currentValue = this.value ? this.value : 0;

            if (this.options.precision)
                newValue = parseFloat(this._toFixed(currentValue + step, this.options.precision));
            else
                newValue = parseInt(currentValue + step);

            if (this.options.min != undefined && newValue < this.options.min) {
                newValue = this.options.min;
            }

            if (this.options.max != undefined && newValue > this.options.max) {
                newValue = this.options.max;
            }

            this.element.val(newValue).attr('aria-valuenow', newValue);
            this.value = newValue;

            this.element.trigger('change');
        },

        _updateValue: function () {
            var value = this.element.val();

            if (value == '') {
                if (this.options.min != undefined)
                    this.value = this.options.min;
                else
                    this.value = 0;
            }
            else {
                if (this.options.step)
                    value = parseFloat(value);
                else
                    value = parseInt(value);

                if (!isNaN(value)) {
                    this.value = value;
                }
            }
        },

        _initValue: function () {
            var value = this.element.val();

            if (value == '') {
                if (this.options.min != undefined)
                    this.value = this.options.min;
                else
                    this.value = 0;
            }
            else {
                if (this.options.prefix)
                    value = value.split(this.options.prefix)[1];

                if (this.options.suffix)
                    value = value.split(this.options.suffix)[0];

                if (this.options.step)
                    this.value = parseFloat(value);
                else
                    this.value = parseInt(value);
            }
        },

        _format: function () {
            var value = this.value;

            if (this.options.prefix)
                value = this.options.prefix + value;

            if (this.options.suffix)
                value = value + this.options.suffix;

            this.element.val(value);
        }
    });
});
/**
 * PrimeFaces SplitButton Widget
 */
$(function () {

    $.widget("primeui.puisplitbutton", {

        options: {
            icon: null, iconPos: 'left',
            items: null
        },

        _create: function () {
            this.element.wrap('<div class="pui-splitbutton pui-buttonset ui-widget"></div>');
            this.container = this.element.parent().uniqueId();
            this.menuButton = this.container.append('<button class="pui-splitbutton-menubutton" type="button"></button>').children('.pui-splitbutton-menubutton');
            this.options.disabled = this.element.prop('disabled');

            if (this.options.disabled) {
                this.menuButton.prop('disabled', true);
            }

            this.element.puibutton(this.options).removeClass('ui-corner-all').addClass('ui-corner-left');
            this.menuButton.puibutton({
                icon: 'ui-icon-triangle-1-s'
            }).removeClass('ui-corner-all').addClass('ui-corner-right');

            if (this.options.items && this.options.items.length) {
                this._renderPanel();
            }

            this._bindEvents();
        },

        _renderPanel: function () {
            this.menu = $('<div class="pui-menu pui-menu-dynamic ui-widget ui-widget-content ui-corner-all ui-helper-clearfix pui-shadow"></div>').
                append('<ul class="pui-menu-list ui-helper-reset"></ul>');
            this.menuList = this.menu.children('.pui-menu-list');

            for (var i = 0; i < this.options.items.length; i++) {
                var item = this.options.items[i],
                    menuitem = $('<li class="pui-menuitem ui-widget ui-corner-all" role="menuitem"></li>'),
                    link = $('<a class="pui-menuitem-link ui-corner-all"><span class="pui-menuitem-icon ui-icon ' + item.icon + '"></span><span class="ui-menuitem-text">' + item.text + '</span></a>');

                if (item.url) {
                    link.attr('href', item.url);
                }

                if (item.click) {
                    link.on('click.puisplitbutton', item.click);
                }

                menuitem.append(link).appendTo(this.menuList);
            }

            this.menu.appendTo(this.options.appendTo || this.container);

            this.options.position = {
                my: 'left top', at: 'left bottom', of: this.element
            };
        },

        _bindEvents: function () {
            var $this = this;

            this.menuButton.on('click.puisplitbutton', function () {
                if ($this.menu.is(':hidden'))
                    $this.show();
                else
                    $this.hide();
            });

            this.menuList.children().on('mouseover.puisplitbutton',function (e) {
                $(this).addClass('ui-state-hover');
            }).on('mouseout.puisplitbutton',function (e) {
                    $(this).removeClass('ui-state-hover');
                }).on('click.puisplitbutton', function () {
                    $this.hide();
                });

            $(document.body).bind('mousedown.' + this.container.attr('id'), function (e) {
                if ($this.menu.is(":hidden")) {
                    return;
                }

                var target = $(e.target);
                if (target.is($this.element) || $this.element.has(target).length > 0) {
                    return;
                }

                var offset = $this.menu.offset();
                if (e.pageX < offset.left ||
                    e.pageX > offset.left + $this.menu.width() ||
                    e.pageY < offset.top ||
                    e.pageY > offset.top + $this.menu.height()) {

                    $this.element.removeClass('ui-state-focus ui-state-hover');
                    $this.hide();
                }
            });

            var resizeNS = 'resize.' + this.container.attr('id');
            $(window).unbind(resizeNS).bind(resizeNS, function () {
                if ($this.menu.is(':visible')) {
                    $this._alignPanel();
                }
            });
        },

        show: function () {
            this._alignPanel();
            this.menuButton.trigger('focus');
            this.menu.show();
            this._trigger('show', null);
        },

        hide: function () {
            this.menuButton.removeClass('ui-state-focus');
            this.menu.fadeOut('fast');
            this._trigger('hide', null);
        },

        _alignPanel: function () {
            this.menu.css({left: '', top: '', 'z-index': ++PUI.zindex}).position(this.options.position);
        }
    });
});
/**
 * PrimeUI sticky widget
 */
$(function () {

    $.widget("primeui.puisticky", {

        _create: function () {

            var element = this.element;

            this.initialState = {
                top: element.offset().top,
                width: element.width(),
                height: element.height()
            };

            var win = $(window),
                $this = this;

            win.on('scroll', function () {
                if (win.scrollTop() > $this.initialState.top) {
                    $this._fix();
                }
                else {
                    $this._restore();
                }
            });

        },

        _refresh: function () {
            $(window).off('scroll');

            this._create();
        },

        _fix: function () {
            if (!this.fixed) {
                this.element.css({
                    'position': 'fixed',
                    'top': 0,
                    'z-index': 10000,
                    'width': this.initialState.width
                })
                    .addClass('pui-shadow ui-sticky');

                $('<div class="ui-sticky-ghost"></div>').height(this.initialState.height).insertBefore(this.element);

                this.fixed = true;
            }
        },


        _restore: function () {
            if (this.fixed) {
                this.element.css({
                    position: 'static',
                    top: 'auto',
                    'width': this.initialState.width
                })
                    .removeClass('pui-shadow ui-sticky');

                this.element.prev('.ui-sticky-ghost').remove();

                this.fixed = false;
            }

        }

    });

});
/**
 * PrimeUI tabview widget
 */
$(function () {

    $.widget("primeui.puitabview", {

        options: {
            activeIndex: 0, orientation: 'top'
        },

        _create: function () {
            var element = this.element;

            element.addClass('pui-tabview ui-widget ui-widget-content ui-corner-all ui-hidden-container')
                .children('ul').addClass('pui-tabview-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all')
                .children('li').addClass('ui-state-default ui-corner-top');

            element.addClass('pui-tabview-' + this.options.orientation);

            element.children('div').addClass('pui-tabview-panels').children().addClass('pui-tabview-panel ui-widget-content ui-corner-bottom');

            element.find('> ul.pui-tabview-nav > li').eq(this.options.activeIndex).addClass('pui-tabview-selected ui-state-active');
            element.find('> div.pui-tabview-panels > div.pui-tabview-panel:not(:eq(' + this.options.activeIndex + '))').addClass('ui-helper-hidden');

            this.navContainer = element.children('.pui-tabview-nav');
            this.panelContainer = element.children('.pui-tabview-panels');

            this._bindEvents();
        },

        _bindEvents: function () {
            var $this = this;

            //Tab header events
            this.navContainer.children('li')
                .on('mouseover.tabview', function (e) {
                    var element = $(this);
                    if (!element.hasClass('ui-state-disabled') && !element.hasClass('ui-state-active')) {
                        element.addClass('ui-state-hover');
                    }
                })
                .on('mouseout.tabview', function (e) {
                    var element = $(this);
                    if (!element.hasClass('ui-state-disabled') && !element.hasClass('ui-state-active')) {
                        element.removeClass('ui-state-hover');
                    }
                })
                .on('click.tabview', function (e) {
                    var element = $(this);

                    if ($(e.target).is(':not(.ui-icon-close)')) {
                        var index = element.index();

                        if (!element.hasClass('ui-state-disabled') && index != $this.options.selected) {
                            $this.select(index);
                        }
                    }

                    e.preventDefault();
                });

            //Closable tabs
            this.navContainer.find('li .ui-icon-close')
                .on('click.tabview', function (e) {
                    var index = $(this).parent().index();

                    $this.remove(index);

                    e.preventDefault();
                });
        },

        select: function (index) {
            this.options.selected = index;

            var newPanel = this.panelContainer.children().eq(index),
                headers = this.navContainer.children(),
                oldHeader = headers.filter('.ui-state-active'),
                newHeader = headers.eq(newPanel.index()),
                oldPanel = this.panelContainer.children('.pui-tabview-panel:visible'),
                $this = this;

            //aria
            oldPanel.attr('aria-hidden', true);
            oldHeader.attr('aria-expanded', false);
            newPanel.attr('aria-hidden', false);
            newHeader.attr('aria-expanded', true);

            if (this.options.effect) {
                oldPanel.hide(this.options.effect.name, null, this.options.effect.duration, function () {
                    oldHeader.removeClass('pui-tabview-selected ui-state-active');

                    newHeader.removeClass('ui-state-hover').addClass('pui-tabview-selected ui-state-active');
                    newPanel.show($this.options.name, null, $this.options.effect.duration, function () {
                        $this._trigger('change', null, index);
                    });
                });
            }
            else {
                oldHeader.removeClass('pui-tabview-selected ui-state-active');
                oldPanel.hide();

                newHeader.removeClass('ui-state-hover').addClass('pui-tabview-selected ui-state-active');
                newPanel.show();

                this._trigger('change', null, index);
            }
        },

        remove: function (index) {
            var header = this.navContainer.children().eq(index),
                panel = this.panelContainer.children().eq(index);

            this._trigger('close', null, index);

            header.remove();
            panel.remove();

            //active next tab if active tab is removed
            if (index == this.options.selected) {
                var newIndex = this.options.selected == this.getLength() ? this.options.selected - 1 : this.options.selected;
                this.select(newIndex);
            }
        },

        getLength: function () {
            return this.navContainer.children().length;
        },

        getActiveIndex: function () {
            return this.options.selected;
        },

        _markAsLoaded: function (panel) {
            panel.data('loaded', true);
        },

        _isLoaded: function (panel) {
            return panel.data('loaded') == true;
        },

        disable: function (index) {
            this.navContainer.children().eq(index).addClass('ui-state-disabled');
        },

        enable: function (index) {
            this.navContainer.children().eq(index).removeClass('ui-state-disabled');
        }

    });
});
/**
 * PrimeUI Terminal widget
 */
$(function () {

    $.widget("primeui.puiterminal", {

        options: {
            welcomeMessage: '',
            prompt: '#',
            handler: null
        },

        _create: function () {
            this.element.addClass('pui-terminal ui-widget ui-widget-content ui-corner-all')
                .append('<div>' + this.options.welcomeMessage + '</div>')
                .append('<div class="pui-terminal-content"></div>')
                .append('<div><span class="pui-terminal-prompt">' + this.options.prompt + '</span>' +
                    '<input type="text" class="pui-terminal-input" autocomplete="off"></div>');

            this.promptContainer = this.element.find('> div:last-child > span.pui-terminal-prompt');
            this.content = this.element.children('.pui-terminal-content');
            this.input = this.promptContainer.next();
            this.commands = [];
            this.commandIndex = 0;

            this._bindEvents();
        },

        _bindEvents: function () {
            var $this = this;

            this.input.on('keydown.terminal', function (e) {
                var keyCode = $.ui.keyCode;

                switch (e.which) {
                    case keyCode.UP:
                        if ($this.commandIndex > 0) {
                            $this.input.val($this.commands[--$this.commandIndex]);
                        }

                        e.preventDefault();
                        break;

                    case keyCode.DOWN:
                        if ($this.commandIndex < ($this.commands.length - 1)) {
                            $this.input.val($this.commands[++$this.commandIndex]);
                        }
                        else {
                            $this.commandIndex = $this.commands.length;
                            $this.input.val('');
                        }

                        e.preventDefault();
                        break;

                    case keyCode.ENTER:
                    case keyCode.NUMPAD_ENTER:
                        $this._processCommand();

                        e.preventDefault();
                        break;
                }
            });
        },

        _processCommand: function () {
            var command = this.input.val();
            this.commands.push();
            this.commandIndex++;

            if (this.options.handler && $.type(this.options.handler) === 'function') {
                this.options.handler.call(this, command, this._updateContent);
            }
        },

        _updateContent: function (content) {
            var commandResponseContainer = $('<div></div>');
            commandResponseContainer.append('<span>' + this.options.prompt + '</span><span class="pui-terminal-command">' + this.input.val() + '</span>')
                .append('<div>' + content + '</div>').appendTo(this.content);

            this.input.val('');
            this.element.scrollTop(this.content.height());
        },

        clear: function () {
            this.content.html('');
            this.input.val('');
        }
    });
});
/**
 * PrimeFaces Tooltip Widget
 */
$(function () {

    $.widget("primeui.puitooltip", {

        options: {
            showEvent: 'mouseover',
            hideEvent: 'mouseout',
            showEffect: 'fade',
            hideEffect: null,
            showEffectSpeed: 'normal',
            hideEffectSpeed: 'normal',
            my: 'left top',
            at: 'right bottom',
            showDelay: 150
        },

        _create: function () {
            this.options.showEvent = this.options.showEvent + '.puitooltip';
            this.options.hideEvent = this.options.hideEvent + '.puitooltip';

            if (this.element.get(0) === document) {
                this._bindGlobal();
            }
            else {
                this._bindTarget();
            }
        },

        _bindGlobal: function () {
            this.container = $('<div class="pui-tooltip pui-tooltip-global ui-widget ui-widget-content ui-corner-all pui-shadow" />').appendTo(document.body);
            this.globalSelector = 'a,:input,:button,img';
            var $this = this;

            $(document).off(this.options.showEvent + ' ' + this.options.hideEvent, this.globalSelector)
                .on(this.options.showEvent, this.globalSelector, null, function () {
                    var target = $(this),
                        title = target.attr('title');

                    if (title) {
                        $this.container.text(title);
                        $this.globalTitle = title;
                        $this.target = target;
                        target.attr('title', '');
                        $this.show();
                    }
                })
                .on(this.options.hideEvent, this.globalSelector, null, function () {
                    var target = $(this);

                    if ($this.globalTitle) {
                        $this.container.hide();
                        target.attr('title', $this.globalTitle);
                        $this.globalTitle = null;
                        $this.target = null;
                    }
                });

            var resizeNS = 'resize.puitooltip';
            $(window).unbind(resizeNS).bind(resizeNS, function () {
                if ($this.container.is(':visible')) {
                    $this._align();
                }
            });
        },

        _bindTarget: function () {
            this.container = $('<div class="pui-tooltip ui-widget ui-widget-content ui-corner-all pui-shadow" />').appendTo(document.body);

            var $this = this;
            this.element.off(this.options.showEvent + ' ' + this.options.hideEvent)
                .on(this.options.showEvent, function () {
                    $this.show();
                })
                .on(this.options.hideEvent, function () {
                    $this.hide();
                });

            this.container.html(this.options.content);

            this.element.removeAttr('title');
            this.target = this.element;

            var resizeNS = 'resize.' + this.element.attr('id');
            $(window).unbind(resizeNS).bind(resizeNS, function () {
                if ($this.container.is(':visible')) {
                    $this._align();
                }
            });
        },

        _align: function () {
            this.container.css({
                left: '',
                top: '',
                'z-index': ++PUI.zindex
            })
                .position({
                    my: this.options.my,
                    at: this.options.at,
                    of: this.target
                });
        },

        show: function () {
            var $this = this;

            this.timeout = setTimeout(function () {
                $this._align();
                $this.container.show($this.options.showEffect, {}, $this.options.showEffectSpeed);
            }, this.options.showDelay);
        },

        hide: function () {
            clearTimeout(this.timeout);

            this.container.hide(this.options.hideEffect, {}, this.options.hideEffectSpeed, function () {
                $(this).css('z-index', '');
            });
        }
    });
});
/**
 * PrimeUI Tree widget
 */
$(function () {

    $.widget("primeui.puitree", {

        options: {
            nodes: null,
            lazy: false,
            animate: false,
            selectionMode: null,
            icons: null
        },

        _create: function () {
            this.element.uniqueId().addClass('pui-tree ui-widget ui-widget-content ui-corner-all')
                .append('<ul class="pui-tree-container"></ul>');
            this.rootContainer = this.element.children('.pui-tree-container');

            if (this.options.selectionMode) {
                this.selection = [];
            }

            this._bindEvents();

            if ($.type(this.options.nodes) === 'array') {
                this._renderNodes(this.options.nodes, this.rootContainer);
            }
            else if ($.type(this.options.nodes) === 'function') {
                this.options.nodes.call(this, {}, this._initData);
            }
            else {
                throw 'Unsupported type. nodes option can be either an array or a function';
            }
        },

        _renderNodes: function (nodes, container) {
            for (var i = 0; i < nodes.length; i++) {
                this._renderNode(nodes[i], container);
            }
        },

        _renderNode: function (node, container) {
            var leaf = this.options.lazy ? node.leaf : !(node.children && node.children.length),
                iconType = node.iconType || 'def',
                expanded = node.expanded,
                selectable = this.options.selectionMode ? (node.selectable === false ? false : true) : false,
                toggleIcon = leaf ? 'pui-treenode-leaf-icon' :
                    (node.expanded ? 'pui-tree-toggler ui-icon ui-icon-triangle-1-s' : 'pui-tree-toggler ui-icon ui-icon-triangle-1-e'),
                styleClass = leaf ? 'pui-treenode pui-treenode-leaf' : 'pui-treenode pui-treenode-parent',
                nodeElement = $('<li class="' + styleClass + '"></li>'),
                contentElement = $('<span class="pui-treenode-content"></span>');

            nodeElement.data('puidata', node.data).appendTo(container);

            if (selectable) {
                contentElement.addClass('pui-treenode-selectable');
            }

            contentElement.append('<span class="' + toggleIcon + '"></span>')
                .append('<span class="pui-treenode-icon"></span>')
                .append('<span class="pui-treenode-label ui-corner-all">' + node.label + '</span>')
                .appendTo(nodeElement);

            var iconConfig = this.options.icons && this.options.icons[iconType];
            if (iconConfig) {
                var iconContainer = contentElement.children('.pui-treenode-icon'),
                    icon = ($.type(iconConfig) === 'string') ? iconConfig : (expanded ? iconConfig.expanded : iconConfig.collapsed);
                iconContainer.addClass('ui-icon ' + icon);
            }

            if (!leaf) {
                var childrenContainer = $('<ul class="pui-treenode-children"></ul>');
                if (!node.expanded) {
                    childrenContainer.hide();
                }

                childrenContainer.appendTo(nodeElement);

                if (node.children) {
                    for (var i = 0; i < node.children.length; i++) {
                        this._renderNode(node.children[i], childrenContainer);
                    }
                }
            }
        },

        _initData: function (data) {
            this._renderNodes(data, this.rootContainer);
        },

        _handleNodeData: function (data, node) {
            this._renderNodes(data, node.children('.pui-treenode-children'));
            this._showNodeChildren(node);
            node.data('puiloaded', true);
        },

        _bindEvents: function () {
            var $this = this,
                elementId = this.element.attr('id'),
                togglerSelector = '#' + elementId + ' .pui-tree-toggler';

            $(document).off('click.puitree-' + elementId, togglerSelector)
                .on('click.puitree-' + elementId, togglerSelector, null, function (e) {
                    var toggleIcon = $(this),
                        node = toggleIcon.closest('li');

                    if (node.hasClass('pui-treenode-expanded'))
                        $this.collapseNode(node);
                    else
                        $this.expandNode(node);
                });

            if (this.options.selectionMode) {
                var nodeLabelSelector = '#' + elementId + ' .pui-treenode-selectable .pui-treenode-label',
                    nodeContentSelector = '#' + elementId + ' .pui-treenode-selectable.pui-treenode-content';

                $(document).off('mouseout.puitree-' + elementId + ' mouseover.puitree-' + elementId, nodeLabelSelector)
                    .on('mouseout.puitree-' + elementId, nodeLabelSelector, null, function () {
                        $(this).removeClass('ui-state-hover');
                    })
                    .on('mouseover.puitree-' + elementId, nodeLabelSelector, null, function () {
                        $(this).addClass('ui-state-hover');
                    })
                    .off('click.puitree-' + elementId, nodeContentSelector)
                    .on('click.puitree-' + elementId, nodeContentSelector, null, function (e) {
                        $this._nodeClick(e, $(this));
                    });
            }
        },

        expandNode: function (node) {
            this._trigger('beforeExpand', null, {'node': node, 'data': node.data('puidata')});

            if (this.options.lazy && !node.data('puiloaded')) {
                this.options.nodes.call(this, {
                    'node': node,
                    'data': node.data('puidata')
                }, this._handleNodeData);
            }
            else {
                this._showNodeChildren(node);
            }

        },

        collapseNode: function (node) {
            this._trigger('beforeCollapse', null, {'node': node, 'data': node.data('puidata')});

            node.removeClass('pui-treenode-expanded');

            var iconType = node.iconType || 'def',
                iconConfig = this.options.icons && this.options.icons[iconType];
            if (iconConfig && $.type(iconConfig) !== 'string') {
                node.find('> .pui-treenode-content > .pui-treenode-icon').removeClass(iconConfig.expanded).addClass(iconConfig.collapsed);
            }

            var toggleIcon = node.find('> .pui-treenode-content > .pui-tree-toggler'),
                childrenContainer = node.children('.pui-treenode-children');

            toggleIcon.addClass('ui-icon-triangle-1-e').removeClass('ui-icon-triangle-1-s');

            if (this.options.animate)
                childrenContainer.slideUp('fast');
            else
                childrenContainer.hide();

            this._trigger('afterCollapse', null, {'node': node, 'data': node.data('puidata')});
        },

        _showNodeChildren: function (node) {
            node.addClass('pui-treenode-expanded').attr('aria-expanded', true);

            var iconType = node.iconType || 'def',
                iconConfig = this.options.icons && this.options.icons[iconType];
            if (iconConfig && $.type(iconConfig) !== 'string') {
                node.find('> .pui-treenode-content > .pui-treenode-icon').removeClass(iconConfig.collapsed).addClass(iconConfig.expanded);
            }

            var toggleIcon = node.find('> .pui-treenode-content > .pui-tree-toggler');
            toggleIcon.addClass('ui-icon-triangle-1-s').removeClass('ui-icon-triangle-1-e');

            if (this.options.animate)
                node.children('.pui-treenode-children').slideDown('fast');
            else
                node.children('.pui-treenode-children').show();

            this._trigger('afterExpand', null, {'node': node, 'data': node.data('puidata')});
        },

        _nodeClick: function (event, nodeContent) {
            PUI.clearSelection();

            if ($(event.target).is(':not(.pui-tree-toggler)')) {
                var node = nodeContent.parent();

                var selected = this._isNodeSelected(node.data('puidata')),
                    metaKey = event.metaKey || event.ctrlKey;

                if (selected && metaKey) {
                    this.unselectNode(node);
                }
                else {
                    if (this._isSingleSelection() || (this._isMultipleSelection() && !metaKey)) {
                        this.unselectAllNodes();
                    }

                    this.selectNode(node);
                }
            }
        },

        selectNode: function (node) {
            node.attr('aria-selected', true).find('> .pui-treenode-content > .pui-treenode-label').removeClass('ui-state-hover').addClass('ui-state-highlight');
            this._addToSelection(node.data('puidata'));
            this._trigger('nodeSelect', null, {'node': node, 'data': node.data('puidata')});
        },

        unselectNode: function (node) {
            node.attr('aria-selected', false).find('> .pui-treenode-content > .pui-treenode-label').removeClass('ui-state-highlight ui-state-hover');
            this._removeFromSelection(node.data('puidata'));
            this._trigger('nodeUnselect', null, {'node': node, 'data': node.data('puidata')});
        },

        unselectAllNodes: function () {
            this.selection = [];
            this.element.find('.pui-treenode-label.ui-state-highlight').each(function () {
                $(this).removeClass('ui-state-highlight').closest('.ui-treenode').attr('aria-selected', false);
            });
        },

        _addToSelection: function (nodedata) {
            if (nodedata) {
                var selected = this._isNodeSelected(nodedata);
                if (!selected) {
                    this.selection.push(nodedata);
                }
            }
        },

        _removeFromSelection: function (nodedata) {
            if (nodedata) {
                var index = -1;

                for (var i = 0; i < this.selection.length; i++) {
                    var data = this.selection[i];
                    if (data && (JSON.stringify(data) === JSON.stringify(nodedata))) {
                        index = i;
                        break;
                    }
                }

                if (index >= 0) {
                    this.selection.splice(index, 1);
                }
            }
        },

        _isNodeSelected: function (nodedata) {
            var selected = false;

            if (nodedata) {
                for (var i = 0; i < this.selection.length; i++) {
                    var data = this.selection[i];
                    if (data && (JSON.stringify(data) === JSON.stringify(nodedata))) {
                        selected = true;
                        break;
                    }
                }
            }

            return selected;
        },

        _isSingleSelection: function () {
            return this.options.selectionMode && this.options.selectionMode === 'single';
        },

        _isMultipleSelection: function () {
            return this.options.selectionMode && this.options.selectionMode === 'multiple';
        }
    });

});
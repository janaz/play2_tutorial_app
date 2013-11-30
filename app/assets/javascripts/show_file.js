Renderer = function (color, bgcolor, fontStyle) {
    this.getRenderer = function (instance, td) {
        Handsontable.TextCell.renderer.apply(this, arguments);
        $(td).css('background-color', bgcolor).css('color', color).css('font-style', fontStyle);
        return td;
    };
};

ShowFile = function (myData, myHeaders, myPopulation, myNames, modelId) {
    var OPTIONS = myNames;

    var UNKNOWN_RENDERER = new Renderer('white', '#FF5C5C', 'italic').getRenderer;
    var NO_POPULATION_RENDERER = new Renderer('black', '#FFCC00', 'normal').getRenderer;
    var LOW_POPULATION_RENDERER = new Renderer('black', '#FFD83B', 'normal').getRenderer;
    var MEDIUM_POPULATION_RENDERER = new Renderer('black', '#FFE26E', 'normal').getRenderer;
    var HIGH_POPULATION_RENDERER = new Renderer('black', '#FFEFAD', 'normal').getRenderer;
    var FULL_POPULATION_RENDERER = new Renderer('black', '#FFFFFF', 'normal').getRenderer;

    $("#myGrid").handsontable({
        data: myData,

        colHeaders: function (col) {
            var DIV = $('<div><span class="original-value"></span><select id="sel-' + col + '"></select></div>');
            var SELECT = $('select', DIV);
            _(OPTIONS).each(function (el) {
                OPTION = $('<option value="' + el + '">' + el + '</option>');
                if (el == myHeaders[col]) {
                    $(OPTION).attr('selected', true);
                }
                $(SELECT).append($(OPTION));
            });
            return $(DIV).html();
        },
        afterGetColHeader: function (col, TH) {
            var that = this;
            $('select', TH).select2({
                width: 'copy'
            }).on("change", function (e) {
                    myHeaders[col] = e.val;
                    $.ajax({
                        type: "POST",
                        url: "/clustrino/update_columns/"+modelId,
                        data: JSON.stringify(myHeaders),
                        contentType: "application/json; charset=utf-8",
                        success: function(data, textStatus, jqXHR) {
                            console.log('ajax success');
                            console.log(jqXHR);
                            console.log(textStatus);
                            console.log(jqXHR);

                            var el = $('.save-update');
                            el.fadeOut(1, function () {
                                el.html('Changes saved').css('color', 'green');
                                el.fadeIn(500, function () {
                                    el.fadeOut(1000, function () {
                                        el.html('');
                                    });
                                });
                            });
                        },
                        error: function(jqXHR, textStatus, errorThrown) {
                            console.log('ajax error');

                            console.log(jqXHR);
                            console.log(textStatus);
                            console.log(errorThrown);
                            var el = $('.save-update');
                            el.fadeOut(1, function () {
                                el.html('Error saving changes: ').css('color', 'red');
                                el.fadeIn(500, function () {
                                    el.fadeOut(1000, function () {
//                                        el.html('');
                                    });
                                });
                            });
                        }
                    });
//                    console.log(this.columns);
//                    that.columns = _(myHeaders).map(function (el) {
//                        if (el == 'UNKNOWN') {
//                            return {type: {renderer: new Renderer('yellow').getRenderer}};
//                        } else {
//                            return {};
//                        }
//                    });
                    console.log(e);
                    that.render();
//                    $('.original-value', TH).html('change');
                });

        },
        cells: function (row, col, prop) {
            var cellProperties = {};
            if (myHeaders[col] == 'UNKNOWN') {
                cellProperties.renderer = UNKNOWN_RENDERER;
            } else {
                var pop = myPopulation[col];
                if (pop > 0.9) {
                    cellProperties.renderer = FULL_POPULATION_RENDERER;
                } else if (pop > 0.7) {
                    cellProperties.renderer = HIGH_POPULATION_RENDERER;
                } else if (pop > 0.5) {
                    cellProperties.renderer = MEDIUM_POPULATION_RENDERER;
                } else if (pop > 0.2) {
                    cellProperties.renderer = LOW_POPULATION_RENDERER;
                } else {
                    cellProperties.renderer = NO_POPULATION_RENDERER;
                }
            }
            return cellProperties;
        },
//        columns: _(myHeaders).map(function (el) {
//            if (el == 'UNKNOWN') {
//                return {type: {renderer: new Renderer('yellow').getRenderer}};
//            } else {
//                return {};
//            }
//        }),
        autoWrapRow: false,
        columnWidth: 80,
        rowHeaders: true,
        manualColumnResize: true,
        manualColumnMove: false,
        persistentState: false,
        currentRowClassName: null,
        outsideClickDeselects: false,
        removeRowPlugin: false,
        readOnly: true

    });
};


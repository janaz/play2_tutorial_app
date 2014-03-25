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

        colHeaders: myHeaders,
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


Mapping = function (options) {
    console.log(options);

    this.init = function () {
        var that = this;
        _($('.main-select')).each(function(sel){
            var map_id = $(sel).attr('data-mapping-id');
           //    that.populateTypes('mapping_'+id, 'type_'+id);
            $('#mapping_'+map_id).change(function(ev) {
                console.log('changed' +ev + 'calling populateTypes with mapping_'+map_id+ ' type_'+map_id);
                that.populateTypes('mapping_'+map_id, 'type_'+map_id);
            }).change();
        });

        $('.save-changes').click(function(ev) {
           console.log("clicked!");
           var data = {};
           _($('.main-select')).each(function(sel){
                var map_id = $(sel).attr('data-mapping-id');
                var el = $('#mapping_'+map_id);
                var el_typ = $('#type_'+map_id);
                var selected_type = el_typ.find(':selected').attr('data-attribute-type');

                var sel = el.find(':selected');
                var sel_tab = sel.attr('data-core-table');
                var sel_attr = sel.attr('data-core-attribute');
                data[map_id] = {
                    table_name: sel_tab,
                    attribute_name: sel_attr,
                    attribute_type: selected_type
                };
            });
            console.log(data);
            $.ajax({
                type: "POST",
                url: that.getUpdateUrl(),
                data: JSON.stringify(data),
                dataType: 'json',
                contentType: "application/json; charset=utf-8",
                success: function(d, textStatus, jqXHR) {
                    console.log('ajax success');
                    console.log(jqXHR);
                    console.log(textStatus);
                    console.log(jqXHR);
                    $('.info-alert').removeClass('hidden').removeClass('alert-danger').addClass('alert-success').html('Mapping saved successfully').slideDown();
                    $('body').animate({scrollTop:0}, '500');
                },
                error: function(d, textStatus, jqXHR) {
                    var msg = "Unknown error";
                    var resp = $.parseJSON(d.responseText);
                    console.log(resp);
                    if (resp && resp.error) {
                        msg = resp.error;
                    }
                    $('.info-alert').removeClass('hidden').removeClass('alert-success').addClass('alert-danger').html('Error saving mapping: '+msg).slideDown();
                    //$('body').scrollTo('.info-alert');
                    $('body').animate({scrollTop:0}, '500');
                }
            });
           // ev.preventDefault();

        });
    };

    this.getUpdateUrl = function () {
        return this.updateUrl;
    };

    this.populateTypes = function (main_select_id, type_select_id) {
        var el = $('#' + main_select_id);
        var el_typ = $('#' + type_select_id);
        var selected_type = el.attr('data-selected-type');
        var sel = el.find(':selected');
        var sel_tab = sel.attr('data-core-table');
        var sel_attr = sel.attr('data-core-attribute');
        if (sel_tab && sel_attr && options[sel_tab] && options[sel_tab][sel_attr] && options[sel_tab][sel_attr].length > 0) {
            el_typ.empty();
            _(options[sel_tab][sel_attr]).each(function (opt) {
                option = $('<option data-attribute-type="' + opt + '" value="' + opt + '">' + opt + '</option>');
                if (opt == selected_type) {
                    option.attr('selected', true);
                }
                el_typ.append(option);
            });
            el_typ.removeClass('hidden');
        } else {
            el_typ.addClass('hidden');
        }
    };

    this.init();

};


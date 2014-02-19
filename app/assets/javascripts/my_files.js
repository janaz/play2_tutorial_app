MyFiles = function () {

    this.callAjax =  function () {
        var that = this;
        $.ajax({
            type: "GET",
            url: "/neutrino/my_files_json",
            contentType: "application/json; charset=utf-8",
            success: function(data, textStatus, jqXHR) {
                console.log('ajax success');
                console.log(textStatus);
                _(data).each(function(e) {
                    el = "tr.my-file-id-"+e.id;
                    $(el+ " .info-profiling").addClass('hidden');
                    $(el+ " .info-profiling-done").addClass('hidden');
                    $(el+ " .info-auto-mapping").addClass('hidden');
                    $(el+ " .info-manual-mapping").addClass('hidden');
                    if (e.state === "PROFILING") {
                        $(el+ " .info-profiling").removeClass('hidden');
                    }else if (e.state === "PROFILING_DONE") {
                        $(el+ " .info-profiling-done").removeClass('hidden');
                    }else if (e.state === "AUTO_MAPPING") {
                        $(el+ " .info-auto-mapping").removeClass('hidden');
                    }else if (e.state === "AUTO_MAPPING_DONE") {
                        console.log("hey!" + el+ " .info-auto-mapping-done");
                        $(el+ " .info-auto-mapping-done").removeClass('hidden');
                    }
                });
                if ($('tr.upload-add-file').length > 0) {
                    setTimeout(function(){that.callAjax();}, 1000);
                }
            }
        });
    };
    this.callAjax();

};


tinymce.PluginManager.add("insertinput", function (editor, url) {
    editor.ui.registry.addButton("insertinput", {
        icon: "insertinput",
        tooltip: "插入填空",
        onAction: function () {
            // 生成四位随机id 包含数字字母
            function generateRandomId(length = 6) {
                const chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
                let result = "";
                for (let i = 0; i < length; i++) {
                    result += chars.charAt(Math.floor(Math.random() * chars.length));
                }
                return result;
            }

            const id = generateRandomId();
            editor.insertContent(`<flexibleinput fieldid="${id}" contenteditable="false" class="flexible-input-blank" required="true" type="text">        </flexibleinput>`);
        }
    });

    return {
        getMetadata: function () {
            return {
                //插件名和链接会显示在"帮助"→"插件"→"已安装的插件"中
                name: "插入填空", //插件名称
                url: "doc.tduckcloud.com" //作者网址
            };
        }
    };
});

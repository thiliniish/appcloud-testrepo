window.$zopim || (function (d, s) {
    var z = $zopim = function (c) {
        z._.push(c)
    }, $ = z.s =
        d.createElement(s), e = d.getElementsByTagName(s)[0];
    z.set = function (o) {
        z.set._.push(o)
    };
    z._ = [];
    z.set._ = [];
    $.async = !0;
    $.setAttribute("charset", "utf-8");
    $.src = "//v2.zopim.com/?3u5GXGidREH2DDzU8flaiJan1BdOwqNk";
    z.t = +new Date;
    $.type = "text/javascript";
    e.parentNode.insertBefore($, e)
})(document, "script");

$zopim(function () {
    var name = $("#zopim-name").attr('value');
    if (name.indexOf('null') < 0) {
        $zopim.livechat.setName(name);
        $zopim.livechat.setEmail($("#user-email").attr('value'));
    }
});
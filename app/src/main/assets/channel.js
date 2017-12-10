var constructElement = function(tag, options){
    var el = document.createElement(tag);
    el.setAttribute("class", (options.classes||[]).join(" "));
    if(options.text) el.innerText = options.text;
    if(options.html) el.innerHTML = options.html;
    for(var attr in (options.attributes||{})){
        el.setAttribute(attr, options.attributes[attr]);
    }
    for(var tag in (options.elements||{})){
        var sub = constructElement(tag, options.elements[tag]);
        el.appendChild(sub);
    }
    return el;
};

var objectColor = function(object){
    var str = object.toString();
    var hash = 0;
    for(var i=0; i<str.length; i++){
        hash  = ((hash << 5) - hash + str.charCodeAt(i++)) << 0;
    }
    var encoded = hash % 0xFFF;
    var r = 16*(1+(encoded&0xF00)>>8)-1;
    var g = 16*(1+(encoded&0x0F0)>>4)-1;
    var b = 16*(1+(encoded&0x00F)>>0)-1;
    
    return "rgb("+Math.min(200, Math.max(50, r))
        +","+Math.min(180, Math.max(80, g))
        +","+Math.min(180, Math.max(80, b))+")";
};

var formatTime = function(time){
    var date = new Date(time*1000);
    var pd = (a)=>{return (a<10)?"0"+a:""+a;}
    return pd(date.getHours())+":"+pd(date.getMinutes())+":"+pd(date.getSeconds());
};

var showText = function(clock, from, text){
    document.getElementById("channel").appendChild(
        constructElement("div", {
            classes: ["update"],
            elements: {
                time: {
                    classes: ["clock"],
                    text: formatTime(clock)
                },
                a: {
                    classes: ["from"],
                    text: from,
                    attributes: {style: "color:"+objectColor(from)}
                },
                span: {
                    classes: ["text"],
                    html: text
                }
            }
        })
    );
};
var channel = document.getElementById("channel");

var constructElement = function(options){
    var el = document.createElement(options.tag);
    el.setAttribute("class", (options.classes||[]).join(" "));
    if(options.text) el.innerText = options.text;
    if(options.html) el.innerHTML = options.html;
    for(var attr in (options.attributes||{})){
        el.setAttribute(attr, options.attributes[attr]);
    }
    for(var i in (options.elements||[])){
        el.appendChild(constructElement(options.elements[i]));
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
    var pd = function(a){return (a<10)?"0"+a:""+a;}
    return pd(date.getHours())+":"+pd(date.getMinutes())+":"+pd(date.getSeconds());
};

var reloadCSS = function(){
    var links = document.getElementsByTagName("link");
    for(var i in links){
        var link = links[i];
        if(link.rel === "stylesheet"){
            var h = link.href.replace(/(&|\?)forceReload=\d /,"");
            link.href=h+(h.indexOf('?')>=0?'&':'?')+"forceReload="+(new Date().valueOf());
        }
    }
};

var clear = function(){
    while(channel.firstChild){
        channel.removeChild(channel.firstChild);
    }
};

var matchesFrom = function(node, from){
    return node.children[0].children[1].innerText === from;
};

var isAtBottom = function(){
    return (window.innerHeight + window.scrollY) >= document.body.offsetHeight;
};

var lastInserted = null;
var addScrollHandler = function(element){
    if(!isAtBottom()) return element;
    lastInserted = element;
    var elements = element.querySelectorAll("img,audio,video");
    for(var i=0; i<elements.length; i++){
        elements[i].addEventListener("load", function(){
            if(lastInserted === element)
                element.scrollIntoView();
        });
    }
    return element;
};

var constructUpdate = function(update){
    return addScrollHandler(constructElement({
        tag: "div",
        classes: ["update", update.source],
        attributes: {"data-clock": update.clock},
        elements: [{
            tag: "div",
            classes: ["header"],
            elements: [{
                tag: "time",
                classes: ["clock"],
                text: formatTime(update.clock)
            },{
                tag: "a",
                classes: ["from"],
                attributes: (update.source==="self"?{}:{style: "color: "+objectColor(update.from)}),
                text: update.from
            },{
                tag: "span",
                classes: ["text"],
                html: update.text
            }]
        }]
    }));
};

var constructUpdateText = function(update){
    return addScrollHandler(constructElement({
        tag: "div",
        classes: ["content"],
        elements: [{
            tag: "time",
            classes: ["clock"],
            text: formatTime(update.clock)
        },{
            tag: "span",
            classes: ["text"],
            html: update.text
        }]
    }));
};

var insertText = function(update){
    // Ensure in-order insert / append.
    var previous = null;
    var children = channel.children;
    for (var i=0; i<children.length; i++) {
        var child = children[i];
        if(update.clock < parseInt(child.getAttribute("data-clock"))){
            if(previous && matchesFrom(previous, update.from)){
                return previous.appendChild(constructUpdateText(update));
            }else{
                return channel.insertBefore(constructUpdate(update), child);
            }
        }
        previous = child;
    }
    var last = children[children.length-1];
    if(last && matchesFrom(last, update.from)){
        return last.appendChild(constructUpdateText(update));
    }else{
        return channel.appendChild(constructUpdate(update));
    }
}

var showText = function(update){
    var scroll = update.source === "self" || isAtBottom();
    var element = insertText(update);
    if(scroll) element.scrollIntoView();
};

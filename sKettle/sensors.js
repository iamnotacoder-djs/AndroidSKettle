var mraa = require('mraa');
var jsUpmI2cLcd = require('jsupm_i2clcd');

var lv1 = new mraa.Aio(0),
    lv2 = new mraa.Aio(1),
    lv3 = new mraa.Aio(2);
    temp0 = new mraa.Aio(3),
    temp_log = new Array(5),
    temp = 0;
setInterval(function(){
    for(var i=0;i<temp_log.length-1;i++){
        temp_log[i] = temp_log[i+1];
    }
    temp_log[temp_log.length-1] = temp0.read();
    
},500);
setInterval(function(){
    var tmp = 0;
    for(var i=0;i<temp_log.length;i++){
           tmp+= temp_log[i];
        
    }
    temp = tmp/temp_log.length;
    temp -= temp%1;
    temp = (865-temp)*2;
    //console.log(temp+" "+tmp+" "+temp_log.length);
},3000);
module.exports = {
    temp: function(){ return temp; },
    water: function(){
        var lv = 0;
        if(lv1.read()<720)
            lv = 1;
        if(lv2.read()<985)
            lv = 2;
        if(lv3.read()<650)
            lv = 3;
        return lv;
    }
};

var lcd = new jsUpmI2cLcd.Jhd1313m1(0, 0x3E, 0x62);
lcd.setColor(255,255,200);

setInterval(function(){lcd.clear();lcd.write(lv1.read()+" "+lv2.read()+" "+lv3.read()+" "+temp0.read());},300);
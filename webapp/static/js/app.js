//var socket;

/*connect=function connect() {
    socket = io();
    //socket.on('connect', function() {
    socket.emit('my event', {data: 'Im connected!'});
    //    console.log('We')
    //});
}*/


$(document).ready(function(){

    var socket = io();
    var time;
    socket.on('my response', function(msg) {
        socket.emit('server')
        //$('#log').replaceWith('<p>Received: ' + msg.data + '</p>');
        log.innerText = 'Plant Status: ' + msg.data;

        //$('#' + msg.who + 'v').val(msg.data);
    });
    socket.on('overrideon', function(msg) {
        //socket.emit('server')
        //$('#log').replaceWith('<p>Received: ' + msg.data + '</p>');
        override.innerText = 'automation is disabled';
        //$('#' + msg.who + 'v').val(msg.data);
    });

    socket.on('overrideoff', function(msg) {
        //socket.emit('server')
        //$('#log').replaceWith('<p>Received: ' + msg.data + '</p>');
        override.innerText = '';
        //$('#' + msg.who + 'v').val(msg.data);
    });

    

    
        const config = {
            type: 'line',
            data: {
                labels: ['Sensor Data'],
                datasets: [{
                    label: "Sensor Data",
                    backgroundColor: 'rgb(255, 99, 132)',
                    borderColor: 'rgb(255, 99, 132)',
                    data: [],
                    fill: true,
                }],
            },
            options: {
                responsive: true,
                title: {
                    display: true,
                    text: 'Historical 24hr Sensor data'
                },
                tooltips: {
                    mode: 'index',
                    intersect: false,
                },
                hover: {
                    mode: 'nearest',
                    intersect: true
                },
                scales: {
                    xAxes: [{
                        display: true,
                        scaleLabel: {
                            display: true,
                            labelString: 'Time'
                        }
                    }],
                    yAxes: [{
                        display: true,
                        scaleLabel: {
                            display: true,
                            labelString: 'Value'
                        }
                    }]
                }
            }
        };

        // Get weather data for seattle
        $.getJSON('http://api.openweathermap.org/data/2.5/weather?q=Seattle&appid=26bac2f5c6fae77e59ff9cc45e16fbaf&units=metric', function(wjdata) {
            //const wjdata = JSON.parse(wdata);
            const icon_type = 'http://openweathermap.org/img/wn/' + wjdata.weather[0].icon + '@2x.png';
            $("#wicon").attr("src", icon_type);
            weather.innerText = '' + '         Hi: ' + wjdata.main.temp_min + ' \n       Low: ' + wjdata.main.temp_max;
         }); 

         /* Get data, the past 24hrs
         var data24 = [];
         var d = new Date(new Date() -  (60 * 60 * 24 * 1000));
         var n = d.toISOString();
         n = n.substr(0,9)
         for (var i = 0; i < 24; i++) {
             //data24[i] = $.getJSON(('https://tg3po98xd3.execute-api.us-east-2.amazonaws.com/dev/plantdata/?time=' + n + ' ' + i).toString())
             data24[i] = $.getJSON("https://tg3po98xd3.execute-api.us-east-2.amazonaws.com/dev/plantdata/?time=2021-05-16+16");
         }*/
        


        
        

        const context = document.getElementById('canvas').getContext('2d');

        const lineChart = new Chart(context, config);

        

        if (selectChart = "temp") {

            //config.data.labels.push(jdata.time);
            //config.data.datasets[0].data.push(jdata.temp);
            //lineChart.update();
        }

        //const source = new EventSource("/chart-data");

        /*source.onmessage = function (event) {
            const data = JSON.parse(event.data);
            if (config.data.labels.length === 20) {
                config.data.labels.shift();
                config.data.datasets[0].data.shift();
            }
            config.data.labels.push(data.time);
            config.data.datasets[0].data.push(data.value);
            lineChart.update();
        }*/
    

    
    socket.on('client', function(data) {
        // 'time' 'temp' 'humidity' 'pressure' 'soilmoist' 'lightlevel'
        const jdata = JSON.parse(data);
        temp.innerText = 'Temp: ' + jdata.temp + ' C°';
        humidity.innerText = 'Humidity: ' + jdata.humidity + ' %';
        pressure.innerText = 'Baro. Pressure: ' + jdata.pressure + ' hPa';
        soilmoisture.innerText = 'Soil Moisture: ' + jdata.soilmoist + ' % ';
        lightlevel.innerText = 'Light Level: ' + jdata.lightlevel + ' % ';
        timed.innerText = 'Server Time: ' + jdata.time;
        //time = jdata.time.substr(0,9);

        if(selectChart == "tempnow") {
            if (config.data.labels.length === 75) {
                config.data.labels.shift();
                config.data.datasets[0].data.shift();
            }
            config.data.labels.push(jdata.time);
            config.data.datasets[0].data.push(jdata.temp);
            lineChart.update();
        }

    });

    // Chart data
    socket.on('chart_data', function(msg) {
        // 'time' 'temp' 'humidity' 'pressure' 'soilmoist' 'lightlevel'
        const sdata = JSON.parse(msg);
        console.log(sdata[0]);
        console.log(sdata[1]);
        config.data.labels = (sdata[0]);
        config.data.datasets[0].data = sdata[1];
        lineChart.update();
  

    });

    $('#fanoff').click(function(event){
        socket.emit('fanoff');
    });

    $('#fanon').click(function(event){
        socket.emit('fanon');
    });

    $('#lighton').click(function(event){
        socket.emit('lighton');
    });

    $('#lightoff').click(function(event){
        socket.emit('lightoff');
    });

    $('#dispense').click(function(event){
        socket.emit('water');
    });

    $('input.sync').on('input', function(event) {
        socket.emit('Slider value changed', {
            who: $(this).attr('id'),
            data: $(this).val()
        });
        return false;
    });


    $("#selectchart").change(function() {
        socket.emit('chart', $('#selectchart').val());
        console.log('Got chart data');
    })

    socket.on('update value', function(msg) {
        console.log('Slider value updated');
        $('#' + msg.who).val(msg.data);
        if (msg.who != 'temps') {
            $('#' + msg.who + 'v').val(msg.data + '%');
        } else {
            $('#' + msg.who + 'v').val(msg.data + ' °C');
        }


    });
    
});


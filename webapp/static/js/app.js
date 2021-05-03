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
    socket.on('my response', function(msg) {
        socket.emit('server')
        //$('#log').replaceWith('<p>Received: ' + msg.data + '</p>');
        log.innerText = 'Plant Status: ' + msg.data;
    });

    $(document).ready(function () {
        const config = {
            type: 'line',
            data: {
                labels: [],
                datasets: [{
                    label: "Random Dataset",
                    backgroundColor: 'rgb(255, 99, 132)',
                    borderColor: 'rgb(255, 99, 132)',
                    data: [],
                    fill: false,
                }],
            },
            options: {
                responsive: true,
                title: {
                    display: true,
                    text: 'This is a realtime chart'
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

        const context = document.getElementById('canvas').getContext('2d');

        const lineChart = new Chart(context, config);

        const source = new EventSource("/chart-data");

        source.onmessage = function (event) {
            const data = JSON.parse(event.data);
            if (config.data.labels.length === 20) {
                config.data.labels.shift();
                config.data.datasets[0].data.shift();
            }
            config.data.labels.push(data.time);
            config.data.datasets[0].data.push(data.value);
            lineChart.update();
        }
    });

    
    socket.on('client', function(data) {
        // 'time' 'temp' 'humidity' 'pressure' 'soilmoist' 'lightlevel'
        const jdata = JSON.parse(data);
        timed.innerText = 'The date and time on the server is: ' + jdata.time;
        temp.innerText = 'Temp: ' + jdata.temp + ' C°';
        humidity.innerText = 'Humidity: ' + jdata.humidity + ' %';
        pressure.innerText = 'Baro. Pressure: ' + jdata.pressure + ' hPa';
    });
    
});


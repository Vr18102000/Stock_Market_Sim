// Establish connection to WebSocket endpoint
var socket = new SockJS('/stock-websocket');
var stompClient = Stomp.over(socket);

// Connect to the WebSocket server
stompClient.connect({}, function (frame) {
    console.log('Connected: ' + frame);

    // Subscribe to stock updates from the server
    stompClient.subscribe('/topic/stockPrices', function (message) {
        var stock = JSON.parse(message.body);  // Parse the stock data
        updateStockBox(stock.symbol, stock.price);  // Update the UI with the stock data
    });
});

// Function to update the stock box in the UI
function updateStockBox(symbol, price) {
    var stockBox = document.getElementById(symbol);

    if (!stockBox) {
        // Create a new stock box if it doesn't exist
        stockBox = document.createElement('div');
        stockBox.id = symbol;
        stockBox.classList.add('stock-box');
        stockBox.innerHTML = `<h3>${symbol}</h3><p>$${price}</p>`;
        document.getElementById('stocks-container').appendChild(stockBox);
    } else {
        // Update the price in the existing stock box
        let priceElement = stockBox.querySelector('p');
        let oldPrice = parseFloat(priceElement.innerText.replace('$', ''));
        if (oldPrice !== price) {
            // Add blinking effect if the price has changed
            stockBox.classList.add('blink');
            setTimeout(() => stockBox.classList.remove('blink'), 500);
        }
        priceElement.innerText = `$${price}`;
    }
}

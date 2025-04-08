
function delayedValue(value, delay) {
    setTimeout(() => {
        console.log(value);
        console.log("end demo")
    }, delay)
}

console.log("start demo")
delayedValue(45, 3000)
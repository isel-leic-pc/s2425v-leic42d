
function delayedValue(value, delay) {
    return new Promise((resolve, reject) => {
        // after 'delay' milliseconds, fulfill the promise with 'value'
        setTimeout(() => { resolve(value) }, delay)
    })
}

async function getValue() {
    const value = await delayedValue(1234, 4000)
    console.log(`>> value produced: ${ value } <<`)
    return value
}

const promise = getValue()
console.log(":: waiting... ::")
const value = await promise
console.log(`>> value produced: ${ value } <<`)

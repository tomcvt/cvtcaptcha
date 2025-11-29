
/*
usage: 

CaptchaModule.Captcha({
    type: 'CLICK_IN_ORDER',
    createEndpoint: '/api/captcha/create',
    solveEndpoint: '/api/captcha/solve',
    domainOrigin: 'yourdomain.com',
    onSuccess: (data, captchaToken) => {
        alert('Captcha solved! Data: ' + JSON.stringify(data));
        console.log('Received Captcha Token: ', captchaToken);
        verifyToken(captchaToken);
    }
}).renderInto('#captchaContainer');


*/


const CaptchaModule = {
    Captcha: function (config) {
        let captchaData = null;
        let cvtCaptchaToken = null;
        let solved = null;
        let userSolution = "";
        let markers = [];
        let markerElements = [];
        let doOnSuccess = null;
        let createEndpoint = config.createEndpoint ? config.createEndpoint : '/api/captcha/create';
        let solveEndpoint = config.solveEndpoint ? config.solveEndpoint : '/api/captcha/solve';
        let domainOrigin = config.domainOrigin ? config.domainOrigin : 'https://captcha.tomcvt.pl'; //to change later
        let requestId = "00000000-0000-0000-0000-000000000000";
        if (!config || !config.type) {
            throw new Error('Captcha type is required in config');
        }
        if (config.type !== 'CLICK_IN_ORDER') {
            throw new Error('Unsupported captcha type: ' + config.type);
        }

        const fetchCaptcha = (async () => {
            const response = await fetch(`${domainOrigin}${createEndpoint}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ requestId: requestId, type: config.type })
            });
            if (!response.ok) {
                throw new Error('Failed to fetch captcha data');
            }
            captchaData = await response.json();
            requestId = captchaData.requestId;
            captchaData.requiredClicks = captchaData.requiredClicks || 4;
            return captchaData;
        })();

        const verifyCaptcha = async (solution) => {
            const response = await fetch(`${domainOrigin}${solveEndpoint}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ requestId: requestId, type: config.type, solution: solution })
            });
            if (!response.ok) {
                console.log(await response.text());
                return false;
            }
            const result = await response.json();
            cvtCaptchaToken = result.cvtCaptchaToken;
            return true;
        };

        if (config.onSuccess && typeof config.onSuccess === 'function') {
            doOnSuccess = config.onSuccess;
        }

        const imgWrapper = document.createElement('div');
        imgWrapper.style.position = 'relative';
        imgWrapper.style.width = '400px';
        imgWrapper.style.height = '300px';
        const img = document.createElement('img');
        img.width = 400;
        img.height = 300;


        // if CLICK_IN_ORDER, handle clicks
        if (config.type === 'CLICK_IN_ORDER') {
            document.addEventListener('click', function (event) {
                if (img && event.target === img && config.type === 'CLICK_IN_ORDER') {
                    const rect = imgWrapper.getBoundingClientRect();
                    const x = event.clientX - rect.left;
                    const y = event.clientY - rect.top;
                    const index = markers.length + 1;
                    const marker = createMarker(x, y, index);
                    imgWrapper.appendChild(marker);
                    markerElements.push(marker);
                    markers.push({ x: x, y: y });
                    const relX = x / img.width;
                    const relY = y / img.height;
                    userSolution += relX.toFixed(4) + ',' + relY.toFixed(4) + ';';

                    if (markers.length >= captchaData.requiredClicks) {
                        verifyCaptchaAndGetToken(userSolution);
                    }
                }
            });
        }

        async function verifyCaptchaAndGetToken(solution) {
            const isSolved = await verifyCaptcha(userSolution);
            if (isSolved) {
                alert('Captcha solved successfully!');
                if (doOnSuccess) {
                    doOnSuccess({ requestId: requestId, type: config.type, solution: userSolution }, cvtCaptchaToken);
                }
            } else {
                alert('Captcha solution incorrect. Please try again.');
            }
            resetCaptcha();
        }


        function resetCaptcha() {
            markerElements.forEach(marker => {
                imgWrapper.removeChild(marker);
            });
            markerElements = [];
            markers = [];
            userSolution = "";
        }

        //Return an object with methods to interact with the captcha
        return {
            renderInto: async function (containerSelector) {
                await fetchCaptcha;
                const container = document.querySelector(containerSelector);
                container.style.width = (img.width + 10) + 'px';
                container.style.height = (img.height + 60) + 'px';
                const moduleBox = document.createElement('div');
                moduleBox.style.border = '1px solid #ccc';
                moduleBox.style.padding = '5px';
                moduleBox.style.position = 'relative';
                moduleBox.style.width = img.width + 'px';
                moduleBox.style.height = (img.height + 50) + 'px';
                moduleBox.style.display = 'flex';
                moduleBox.style.flexDirection = 'column';
                moduleBox.style.gap = '10px';
                moduleBox.style.alignItems = 'center';
                moduleBox.style.justifyContent = 'center';
                container.appendChild(moduleBox);
                if (!container) {
                    throw new Error('Container not found for selector: ' + containerSelector);
                }
                //Render captcha based on type
                if (config.type === 'CLICK_IN_ORDER') {
                    //For now log the solution
                    console.log('Captcha Solution (for testing):', captchaData.imageUrl);
                    img.src = captchaData.imageUrl;
                    imgWrapper.appendChild(img);
                    moduleBox.appendChild(imgWrapper);
                    //Additional logic to handle click in order can be added here
                }
                const submitButton = document.createElement('button');
                submitButton.textContent = 'Submit Captcha';
                submitButton.onclick = async () => {
                    await verifyCaptchaAndGetToken(userSolution);
                };
                moduleBox.appendChild(submitButton);
            },
            isSolved: function () {
                if (solved !== null) {
                    return solved;
                }
                throw new Error('Captcha has not been verified yet');
            }
        };
    }
}


function createMarker(x, y, index) {
    const marker = document.createElement('div');
    marker.style.position = 'absolute';
    marker.style.left = (x - 10) + 'px';
    marker.style.top = (y - 10) + 'px';
    marker.style.width = '20px';
    marker.style.height = '20px';
    marker.style.backgroundColor = 'red';
    marker.style.borderRadius = '50%';
    marker.style.color = 'white';
    marker.style.display = 'flex';
    marker.style.alignItems = 'center';
    marker.style.justifyContent = 'center';
    marker.textContent = index;
    return marker;
}

//Example usage:
//const captcha = CaptchaModule.Captcha({ type: 'CLICK_IN_ORDER' });
//captcha.render('#captchaDiv'); // Renders when ready
// Later, check if solved:

//if (captcha.isSolved()) {
//    // proceed
//}

if (typeof window !== 'undefined') {
    window.CaptchaModule = CaptchaModule;
}

export default CaptchaModule;
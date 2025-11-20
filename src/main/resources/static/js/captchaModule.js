
/*
usage: CaptchaModule.Captcha({
    type: 'CLICK_IN_ORDER', 
    onSuccess: (data) -> { ... }   //optional, called when captcha is solved, data will contain some info later

});


*/


const CaptchaModule = {
    Captcha: function(config) {
        let captchaData = null;
        let solved = null;
        let userSolution = "";
        let markers = [];
        let doOnSuccess = null;
        let requestId = "00000000-0000-0000-0000-000000000000";
        if (!config || !config.type) {
            throw new Error('Captcha type is required in config');
        }

        const fetchCaptcha = (async () => {
            const response = await fetch('/api/captcha/create', {
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
            return captchaData;
        })();

        const verifyCaptcha = async (solution) => {
            const response = await fetch('/api/captcha/verify', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ requestId: requestId, type: config.type, solution: solution })
            });
            if (!response.ok) {
                throw new Error('Failed to verify captcha solution');
            }
            const result = await response.json();
            solved = result.solved;
            return solved;
        };

        if (config.onSuccess && typeof config.onSuccess === 'function') {
            doOnSuccess = config.onSuccess;
        }

        const img = document.createElement('img');
        img.width = 400;
        img.height = 300;

        document.addEventListener('click', function(event) {
            if (img && event.target === img && config.type === 'CLICK_IN_ORDER') {
                const rect = img.getBoundingClientRect();
                const x = event.clientX - rect.left;
                const y = event.clientY - rect.top;
                const index = markers.length + 1;
                const marker = createMarker(event.clientX, event.clientY, index);
                img.parentElement.appendChild(marker);
                markers.push({ x: x, y: y });
                const relX = x / img.width;
                const relY = y / img.height;
                userSolution += relX.toFixed(4) + ',' + relY.toFixed(4) + ';';
                console.log('Marker added at:', relX.toFixed(4), relY.toFixed(4));
            }
        });

        function resetCaptcha() {
            markers.forEach(marker => {
                if (marker.element && marker.element.parentElement) {
                    marker.element.parentElement.removeChild(marker.element);
                }
            });
            markers = [];
            userSolution = "";
        }

        //Return an object with methods to interact with the captcha
        return {
            render: async function(containerSelector) {
                await fetchCaptcha;
                const container = document.querySelector(containerSelector);
                if (!container) {
                    throw new Error('Container not found for selector: ' + containerSelector);
                }
                //Render captcha based on type
                if (config.type === 'CLICK_IN_ORDER') {
                    //For now log the solution
                    console.log('Captcha Solution (for testing):', captchaData.imageUrl);
                    //const img = document.createElement('img');
                    img.src = captchaData.imageUrl;
                    container.appendChild(img);
                    //Additional logic to handle click in order can be added here
                }
                const submitButton = document.createElement('button');
                submitButton.textContent = 'Submit Captcha';
                submitButton.onclick = async () => {
                    //For testing, we use a dummy solution
                    const isSolved = await verifyCaptcha(userSolution);
                    if (isSolved) {
                        alert('Captcha solved successfully!');
                        if (doOnSuccess) {
                            doOnSuccess({ requestId: requestId, type: config.type, solution: userSolution });
                        }
                    } else {
                        alert('Captcha solution incorrect. Please try again.');
                    }
                    resetCaptcha();
                };
                container.appendChild(submitButton);
            },
            isSolved: function() {
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
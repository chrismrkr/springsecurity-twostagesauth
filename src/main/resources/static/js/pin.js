let generatePin = () => {
    let messengerId = $("input[name='messengerId']").val().trim();
    let messageBody = {"messengerId" : messengerId};

    const token = $("meta[name='_csrf']").attr("content")
    const header = $("meta[name='_csrf_header']").attr("content");

    if(timerObj.getStartTimerChk()) {
        alert("이미 인증 PIN이 발급되었습니다.");
        return;
    }


    $.ajax({
        type: "post",
        url: "/email-confirm",
        data: JSON.stringify(messageBody),
        dataType: "json",

        beforeSend : function(xhr) {
            xhr.setRequestHeader("Content-type","application/json");
            xhr.setRequestHeader(header, token);
            progressPopup.open();
        },
        success: function(data) {
            progressPopup.close();
            alert("인증 PIN이 메일로 전송되었습니다.");
            timerObj.startTimer(180);
        },
        error: function(xhr, status, error) {
            progressPopup.close();
            alert("인증 PIN 생성에 실패했습니다.");
        }
    });
};

let timerObj = (() => {
    let startTimerChk = false;
    return {
        getStartTimerChk() {
            return startTimerChk;
        },
        startTimer(time) {
            startTimerChk = true;
            const timer = document.getElementById('timer');
            let min = parseInt(time/60);
            let sec = time%60;

            let countTimer = setInterval(() => {
                time -= 1;
                if(sec == 0) {
                 if(min != 0) {
                    min -= 1;
                    sec = 59;
                 }
                }
                else {
                    sec -= 1;
                }

                timer.value = min + ":" + (sec >= 10 ? sec : '0'+sec);
                if(time < 0) {
                    clearInterval(countTimer);
                    timer.value = "PIN을 재발급 해주세요.";
                    startTimerChk = false;
                }
            }, 1000);
        }
    }
})();

let progressPopup = (() => {
    return {
        open() {
            debugger;
            let popupObj = document.getElementById('process-popup');
            popupObj.style.display = 'block';
        },
        close() {
            let popupObj = document.getElementById('process-popup');
            popupObj.style.display = 'none';
        }
    }
})();
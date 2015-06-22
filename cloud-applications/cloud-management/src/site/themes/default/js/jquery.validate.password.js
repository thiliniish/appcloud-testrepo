/*
 * jQuery validate.password plug-in 1.0
 *
 * http://bassistance.de/jquery-plugins/jquery-plugin-validate.password/
 *
 * Copyright (c) 2009 Jörn Zaefferer
 *
 * $Id$
 *
 * Dual licensed under the MIT and GPL licenses:
 *   http://www.opensource.org/licenses/mit-license.php
 *   http://www.gnu.org/licenses/gpl.html
 */
(function($) {
	
	
    function rating(rate, message, limit, digit, lowerCase, upperCase, specialCharacter) {
        return {
            rate: rate,
            messageKey: message,
            minimumCharacterLimitExceeded: limit,
            hasDigit: digit,
            hasLowercaseLetter: lowerCase,
            hasUppercaseLetter: upperCase,
            hasSpecialCharacter: specialCharacter
        };
    }
	
    function uncapitalize(str) {
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    function changeImage(image,found) {
        image.removeClass();
        if(found == 1) {
            image.addClass("fa fa-check green");
        } else {
            image.addClass("fa fa-times red");
        }
    }
	
	$.validator.passwordRating = function(password, username, element) {
        var minLength = 8;
        var passwordStrength   = 0;
        var minimumCharacterLimitExceeded = 0;
        var hasDigit = 0;
        var hasLowercaseLetter = 0;
        var hasUppercaseLetter = 0;
        var hasSpecialCharacter = 0;

        if ((password.length > 0) && (password.length <= 7)) {
            passwordStrength = 1;
        }

        if (password.length >= minLength) {
            passwordStrength++;
            minimumCharacterLimitExceeded = 1;
        }
        var item = $('.list1 li:first');
        changeImage(item, minimumCharacterLimitExceeded);

        if ((password.match(/[a-z]/)) && (password.match(/[A-Z]/)) ) {
            passwordStrength++;
        }

        if ((password.match(/[a-z]/))) {
            hasLowercaseLetter = 1;
        }
        var item = $('.list2 li:nth-child(2)');
        changeImage(item, hasLowercaseLetter);

        if ((password.match(/[A-Z]/))) {
            hasUppercaseLetter = 1;
        }
        var item = $('.list2 li:first');
        changeImage(item, hasUppercaseLetter);

        if (password.match(/\d+/)) {
            passwordStrength++;
            hasDigit = 1;
        }
        var item = $('.list2 li:nth-child(3)');
        changeImage(item, hasDigit);

        var re = /(?=.*[-`:<=>;|_+,.?'\"\(\)\[\]\{\}\\\/~!@#$%^&*])/;
        if(re.test(password)){
            hasSpecialCharacter = 1;
        }

        if (password.match(/(?=.*\d)(?=.*[a-z])(?=.*[-`:<=>;|_+,.?'\"\(\)\[\]\{\}\\\/~!@#$%^&*])(?=.*[A-Z]).{8,}/)) {
            passwordStrength++;
        }
        var item = $('.list2 li:nth-child(4)');
        changeImage(item, hasSpecialCharacter);

        if (username && password.toLowerCase() == username.toLowerCase()){
            passwordStrength = 0;
        }

        $('#pwdMeter').removeClass();
        $('#pwdMeter').addClass('neutral');

        switch(passwordStrength){
            case 1:
                return rating(2, "very-weak", minimumCharacterLimitExceeded, hasDigit, hasLowercaseLetter, hasUppercaseLetter, hasSpecialCharacter);
                break;
            case 2:
                return rating(2, "weak", minimumCharacterLimitExceeded, hasDigit, hasLowercaseLetter, hasUppercaseLetter, hasSpecialCharacter);
                break;
            case 3:
                return rating(3, "medium", minimumCharacterLimitExceeded, hasDigit, hasLowercaseLetter, hasUppercaseLetter, hasSpecialCharacter);
                break;
            case 4:
            case 5:
            case 6:
            case 7:
                 return rating(4, "strong", minimumCharacterLimitExceeded, hasDigit, hasLowercaseLetter, hasUppercaseLetter, hasSpecialCharacter);
                 break;
            default:
                return rating(2, "weak", minimumCharacterLimitExceeded, hasDigit, hasLowercaseLetter, hasUppercaseLetter, hasSpecialCharacter);
			}
	}

	$.validator.passwordRating.messages = {
		"similar-to-username": "Too similar to username",
		"very-weak": "Very weak",
		"weak": "Weak",
		"medium": "Medium",
		"strong": "Strong",
		"vstrong": "Very Strong"
	}
	
	$.validator.addMethod("password", function(value, element, usernameField) {
		// use untrimmed value
		var password = element.value,
		// get username for comparison, if specified
			username = $(typeof usernameField != "boolean" ? usernameField : []);
			
        if(!($(element).hasClass("avoidValidate"))){
				
        var rating = $.validator.passwordRating(password, username.val(), element);
        // update message for this field
		
        var meter = $(".password-meter", element.form);
		
        meter.find(".password-meter-bar").removeClass().addClass("password-meter-bar")
        .addClass("password-meter-" + rating.messageKey);
        meter.find(".password-meter-message")
        .removeClass()
        .addClass("password-meter-message")
        .addClass("password-meter-message-" + rating.messageKey)
        .text($.validator.passwordRating.messages[rating.messageKey]);
        // display process bar instead of error message
        var re = /((?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])|(?=.*[a-z])(?=.*[A-Z])(?=.*[-`:<=>;|_+,.?'\"\(\)\[\]\{\}\\\/~!@#$%^&*])|(?=.*[a-z])(?=.*[0-9])(?=.*[-`:<=>;|_+,.?'\"\(\)\[\]\{\}\\\/~!@#$%^&*])|(?=.*[A-Z])(?=.*[0-9])(?=.*[-`:<=>;|_+,.?'\"\(\)\[\]\{\}\\\/~!@#$%^&*])).{8,}/;
        return re.test(value);
    } else{
        return true;
    }
    }, "Minimum system requirements not met.");
	// manually add class rule, to make username param optional
	$.validator.classRuleSettings.password = { password: true };
	
})(jQuery);

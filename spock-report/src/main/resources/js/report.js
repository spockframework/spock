var currentState = "all";
var currentTags = {};
var expandSearches = true;

$(document).ready(function() {
  drawPieCharts();
  computeInheritedTags();
  filterSpecElementsByTagsAndState(currentTags, currentState);

  configureTreeElements();
  configureStateFiltering();
  configureViewSelection();
  configureTagFiltering();
  configureOptions();
});

function drawPieCharts() {
  $("h1 .pie").peity("pie", {
    colours: ['#468847', '#B94A48', '#F89406'],
    diameter: 30
  });
  $("h2 .pie").peity("pie", {
    colours: ['#468847', '#B94A48', '#F89406'],
    diameter: 25
  });
  $("h3 .pie").peity("pie", {
    colours: ['#468847', '#B94A48', '#F89406'],
    diameter: 20
   });
}

function computeInheritedTags() {
  $(".specElement").each(function() {
    var tags = parseTagsString($(this).data("tags"));
    $(this).data("defined-tags", tags);
    $(this).data("inherited-tags", $.extend({}, $(this).parent().closest(".specElement").data("inherited-tags"), tags));
  });
}

function filterSpecElementsByTagsAndState(tags, state) {
  var tagCount = countProperties(tags);

  if (tagCount == 0 && state == "all") {
    $(".specElement").show();
    if (expandSearches) {
      expandToRequirements();
    }
    return;
  }

  var elementsToShow = state == "all" ? $(".specElement") : $(".specElement." + state);

  if (tagCount > 0) {
    elementsToShow = elementsToShow.filter(function() {
      return specElementMatchesSearchTags($(this), $(this).data("inherited-tags"), tags);
    });
  }

  $(".specElement").hide();
  elementsToShow.show();
  elementsToShow.parents(".specElement").show();

  if (expandSearches) {
    expandSearchResults(tags, state, elementsToShow);
  }
}

function expandSearchResults(tags, state, elementsToShow) {
  var elementsToExpandTo = elementsToShow.filter(function() {
    var element = $(this);
    if (element.hasClass(state)) return true;

    var expandTo = false;

    $.each(tags, function(key, allowedValues) {
      var tag = {};
      tag[key] = allowedValues;
      if (specElementMatchesSearchTags(element, element.data("defined-tags"), tag)) {
        expandTo = true;
        return true; // break
      }
    });

    return expandTo;
  });

  collapseElements($(".specElement"));
  expandElements(elementsToExpandTo.parents(".specElement"));
}

function configureTreeElements() {
  $(".elementHeader").click(function() {
    toggleElements($(this).closest(".element"));
  });
}

function configureStateFiltering() {
  $("#state-all").click(function() {
    currentState = "all";
    filterSpecElementsByTagsAndState(currentTags, currentState);
  });
  $("#state-passed").click(function() {
    currentState = "passed";
    filterSpecElementsByTagsAndState(currentTags, currentState);
  });
  $("#state-failed").click(function() {
    currentState = "failed";
    filterSpecElementsByTagsAndState(currentTags, currentState);
  });
  $("#state-skipped").click(function() {
    currentState = "skipped";
    filterSpecElementsByTagsAndState(currentTags, currentState);
  });
}

function configureViewSelection() {
  $("#view-packages").click(function() {
    collapseElements($(".element"));
  });

  $("#view-specifications").click(function() {
    expandElements($(".package"));
    collapseElements($(".element:not(.package)"));
  });

  $("#view-requirements").click(function() {
    expandToRequirements();
  });

  $("#view-requirement-details").click(function() {
    expandElements($(".specElement"));
    collapseElements($(".element:not(.specElement)"));
  });

  $("#view-exceptions").click(function() {
    expandElements($(".specElement:has(.exceptions)"));
    expandElements($(".element.exceptions"));
    collapseElements($(".element:not(.exceptions):not(:has(.exceptions))"));
  });

  $("#view-full-details").click(function() {
    expandElements($(".element"));
  });
}

function configureTagFiltering() {
  $("#tags").multiselect({
    'text': function() {
      return "Tags"
    },
    onchange: function(element, checked) {
      var tagString = element.attr("value");
      var keyValue = tagString.split("=");
      var key = keyValue[0];
      var value = keyValue[1];
      if (checked) {
        if (currentTags[key] == undefined) currentTags[key] = [];
        currentTags[key].push(value);
      } else {
        var idx = currentTags[key].indexOf(value);
        currentTags[key].splice(idx, 1);
        if (currentTags[key].length == 0) {
          delete currentTags[key];
        }
      }
      filterSpecElementsByTagsAndState(currentTags, currentState);
    }
  });
}

function configureOptions() {
  $("#options").multiselect({
    'text': function() {
      return "Options"
    },
    onchange: function(element, checked) {
      switch(element.attr("value")) {
      case "showNarrative":
        $(".narrative").toggle();
        break;
      case "showResults":
        $(".results").toggle();
        break;
      case "expandSearches":
        expandSearches = checked;
        if (checked) {
          filterSpecElementsByTagAndState(currentTags, currentState);
        }
      default:
        console.log("unknown option: " + element.id);
      }
    }
  });
}

function expandToRequirements() {
  expandElements($(".package, .spec"));
  collapseElements($(".element:not(.package):not(.spec)"));
}

function specElementMatchesSearchTags(element, elementTags, searchTags) {
  var match = true;

  $.each(searchTags, function(key, allowedValues) {
    var actualValues = elementTags[key];
    var valueFound = false;
    if (actualValues != undefined) {
      $.each(actualValues, function(idx, actualValue) {
        if ($.inArray(actualValue, allowedValues) > -1) {
          valueFound = true;
          return true; // break
        }
      });
    }
    if (!valueFound) {
      match = false;
      return true; // break
    }
  });

  return match;
}

function parseTagsString(tagsString) {
  var tags = {};
  if (tagsString == undefined) return tags;

  $.each(tagsString.split(","), function(idx, tagString) {
    var keyValue = tagString.split("=");
    var key = keyValue[0];
    var value = keyValue[1];
    if (tags[key] == undefined) tags[key] = [];
    tags[key].push(value);
  });

  return tags;
}

function countProperties(hash) {
  var count = 0;
  $.each(hash, function() {
    count++;
  });
  return count;
}

function expandElements(elements) {
  elements.removeClass("collapsed");
  elements.children(".elementBody").show();
}

function collapseElements(elements) {
  elements.addClass("collapsed");
  elements.children(".elementBody").hide();
}

function toggleElements(elements) {
  elements.toggleClass("collapsed");
  elements.children(".elementBody").toggle();
}



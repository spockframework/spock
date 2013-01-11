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
    $(this).data("inherited-tags", _.extend({}, $(this).parent().closest(".specElement").data("inherited-tags"), tags));
  });
}

function parseTagsString(tagsString) {
  var tags = {};
  if (_.isUndefined(tagsString)) return tags;

  _.each(tagsString.split(","), function(tagString) {
    var keyValue = tagString.split("=");
    var key = keyValue[0];
    var value = keyValue[1];
    if (!_.has(tags, key)) tags[key] = [];
    tags[key].push(value);
  });

  return tags;
}

function filterSpecElementsByTagsAndState(tags, state) {
  var tagCount = _.size(tags);

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

function specElementMatchesSearchTags(element, elementTags, searchTags) {
  return _.every(searchTags, function(allowedValues, key) {
    var actualValues = elementTags[key];
    return !_.isEmpty(_.intersection(actualValues, allowedValues));
  });
}

function expandSearchResults(tags, state, elementsToShow) {
  var elementsToExpandTo = elementsToShow.filter(function() {
    var element = $(this);
    return element.hasClass(state) || _.some(tags, function(allowedValues, key) {
      var tag = _.pick(tags, key);
      return specElementMatchesSearchTags(element, element.data("defined-tags"), tag);
    });
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
        if (!_.has(currentTags, key)) currentTags[key] = [];
        currentTags[key].push(value);
      } else {
        currentTags[key] = _.without(currentTags[key], value);
        if (_.isEmpty(currentTags[key])) {
          currentTags = _.omit(currentTags, key);
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


